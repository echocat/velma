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

ant.copy(
    file: "${project.build.directory}/rpm/${project.artifactId}/RPMS/noarch/${project.artifactId}-${project.properties['rpm.version']}-${project.properties['rpm.release']}.noarch.rpm",
    tofile: "${project.build.directory}/${project.artifactId}-${project.version}.rpm"
);
