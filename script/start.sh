#!/bin/bash
#	Copyright IBM Corporation 2021
#	
#	Licensed under the Eclipse Public License 2.0, Version 2.0 (the "License");
#	you may not use this file except in compliance with the License.
#	
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS,
#	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#	See the License for the specific language governing permissions and
#	limitations under the License.

function usage() {
    echo $1
    cat <<_EOT_
Usage:
    `basename $0` <application github url>
    -h help
_EOT_
    exit 1
}

while getopts "c:h" OPT
do
    case $OPT in
        h) usage "[Help]";;
        \?) usage;;
    esac
done

if [ $# -ne 1 ]; then
    echo "[Error] Application github url is not specified."
    usage
fi

rm -rf ${TCD_APPLICATION_PATH}
git clone $1 ${TCD_APPLICATION_PATH}

${MTA_CLI_PATH}/bin/mta-cli --input ${TCD_APPLICATION_PATH} --sourceMode --target java-ee
# cd tcd-windup && mvn test

${JANUSGRAPH_PATH}/bin/gremlin-server.sh start

until [ -f ${JANUSGRAPH_PATH}/log/gremlin-server.log ]
do
    sleep 1
done

tail -f ${JANUSGRAPH_PATH}/log/gremlin-server.log
