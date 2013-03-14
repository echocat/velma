import static java.io.File.pathSeparator
import static java.lang.System.getenv

/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Velma, Copyright (c) 2011-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 *************************************************************************************** */

public File searchForExecutable (String withName) {
    final def paths = getenv("PATH").split("\\" + pathSeparator);
    def executable = null;
    for (def path : paths) {
        final def potentialDirectory = new File(path);
        final def potentialExecutable = new File(potentialDirectory, withName);
        if (potentialExecutable.isFile()) {
            executable = potentialExecutable;
            break;
        }
    }
    if (executable == null) {
        throw new IllegalStateException("Could not find executable '" + withName + "' is the WIX toolset installed and configured to PATH environment variable?");
    }
    return executable;
}

public String joinCommandLine (String[] arguments) {
    final def sb = new StringBuilder();
    for (def argument : arguments) {
        if (sb.length() > 0) {
            sb.append(' ');
        }
        if (argument.contains(" ") || argument.contains("\"")) {
            sb.append('"').append(argument.replace("\"", "\\\"")).append('"');
        } else {
            sb.append(argument);
        }
    }
    return sb.toString();
}

public void execute (String[] arguments, File workingDirectory) {
    final process = new ProcessBuilder().command(arguments).directory(workingDirectory).start();
    new Thread(new Logger(process.inputStream, false, log)).start();
    new Thread(new Logger(process.errorStream, true, log)).start();
    if (process.waitFor() != 0) {
        throw new IllegalStateException("Got error code " + process.exitValue() + " for: " + joinCommandLine(arguments));
    }
}

public class Logger implements Runnable {

    private final def _inputStream;
    private final def _error;
    private final def _log;

    public Logger (def inputStream, def error, def log) {
        _inputStream = inputStream
        _error = error
        _log = log;
    }

    @Override
    void run () {
        final Reader reader = new InputStreamReader(_inputStream);
        try {
            final BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            while (line != null) {
                if (_error) {
                    _log.error(line.trim());
                } else {
                    _log.info(line.trim());
                }
                line = br.readLine();
            }
        } finally {
            reader.close();
        }
    }
}

final def heat = searchForExecutable("heat.exe");
final def candle = searchForExecutable("candle.exe");
final def light = searchForExecutable("light.exe");
//noinspection GroovyAssignabilityCheck
final def baseDirectory = new File(project.basedir, "src/main/assemble/installers/windows");
//noinspection GroovyAssignabilityCheck
final def buildDirectory = new File(project.build.directory);
final def cacheDirectory = new File(buildDirectory, "wix");
final def filesDirectory = new File(cacheDirectory, "files");
filesDirectory.mkdirs();
final def exeFilesDirectory = new File(cacheDirectory, "exeFiles");
exeFilesDirectory.mkdirs();

log.info("Prepare files in: ${filesDirectory}")
ant.delete(dir: filesDirectory.getCanonicalPath(), quiet: "true");
ant.untar(src: new File(buildDirectory, project.artifactId + "-" + project.version + "-distribution.tar.gz").getCanonicalPath(),
        compression: "gzip",
        dest: filesDirectory.getCanonicalPath(),
        overwrite: "true") {
        patternset() {
            exclude(name: "bin/velma*.exe*")
        }};
ant.delete(dir: exeFilesDirectory.getCanonicalPath(), quiet: "true");
ant.untar(src: new File(buildDirectory, project.artifactId + "-" + project.version + "-distribution.tar.gz").getCanonicalPath(),
        compression: "gzip",
        dest: exeFilesDirectory.getCanonicalPath(),
        overwrite: "true") {
        patternset() {
            include(name: "bin/velma*.exe*")
        }};

log.info("Harvest files from: ${filesDirectory}")
execute((String[]) [
        heat.getCanonicalPath(),
        "dir", filesDirectory.getCanonicalPath(),
        "-nologo", "-sfrag", "-gg", "-g1", "-ke", "-srd", "-sw5150",
        "-cg", "DistributionFiles",
        "-dr", "INSTALLDIR",
        "-var", "var.files",
        "-out", "${cacheDirectory}\\velma.files.wxs",
], baseDirectory);

log.info("Compiling: ${baseDirectory}\\velma.wxs, ${cacheDirectory}\\velma.files.wxs")
execute((String[]) [
        candle.getCanonicalPath(),
        "-nologo",
        "-ext", "WixUIExtension",
        "-ext", "WixUtilExtension",
        "-dfiles=${filesDirectory}",
        "-dexeFiles=${exeFilesDirectory}",
        "-dorganizationName=${project.organization.name}",
        "-dproductName=${project.name}",
        "-dproductShortName=${project.properties['project.shortName']}",
        "-dproductVersion=${project.version}",
        "-dproductUrl=${project.url}",
        "-dproductDescription=${project.description}",
        "${baseDirectory}\\velma.wxs", "${cacheDirectory}\\velma.files.wxs",
        "-out", "${cacheDirectory}\\"
], baseDirectory);

log.info("Linking: ${cacheDirectory}\\*.wixobj")
execute((String[]) [
        light.getCanonicalPath(),
        "-nologo", "-sice:30", "-sw1076",
        "-ext", "WixUIExtension",
        "-ext", "WixUtilExtension",
        "-dfiles=${filesDirectory}",
        "-dexeFiles=${exeFilesDirectory}",
        "-dorganizationName=${project.organization.name}",
        "-dproductName=${project.name}",
        "-dproductShortName=${project.properties['project.shortName']}",
        "-dproductVersion=${project.version}",
        "-dproductUrl=${project.url}",
        "-dproductDescription=${project.description}",
        "-pdbout", "${cacheDirectory}\\velma.wixpdb",
        "${cacheDirectory}\\*.wixobj",
        "-out", "${buildDirectory}\\${project.artifactId}-${project.version}-distribution.msi"
], baseDirectory);

log.info("MSI created: ${buildDirectory}\\velma.msi");
log.info("");
