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
 ****************************************************************************************/

//noinspection GroovyAssignabilityCheck
final def baseDirectory = new File(project.basedir, "src/main/assemble/installers/linux");
final def filesSourceDirectory = new File(baseDirectory, "files");
//noinspection GroovyAssignabilityCheck
final def buildDirectory = new File(project.build.directory);
final def cacheDirectory = new File(buildDirectory, "linux");
final def filesDirectory = new File(cacheDirectory, "files");
filesDirectory.mkdirs();

ant.copy(
        todir: "${filesDirectory}",
        overwrite: "true"
) {
    fileset(dir: filesSourceDirectory)
    filterset() {
        filter(token: "name", value: project.name)
        filter(token: "description", value: project.description)
        filter(token: "artifactId", value: project.artifactId)
        filter(token: "version", value: project.version)
        filter(token: "organization.name", value: project.organization.name)
    }
}
