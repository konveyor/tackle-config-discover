#!/usr/bin/python3
# Copyright IBM Corporation 2021, 2022
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from flask import Flask, request
import subprocess, os, time, signal

app = Flask(__name__)

JANUSGRAPH_PATH = os.environ['JANUSGRAPH_PATH']
TCD_APPLICATION_PATH = os.environ['TCD_APPLICATION_PATH']
MTA_CLI_PATH = os.environ['MTA_CLI_PATH']

@app.route("/collect", methods=['POST', 'GET'])
def collect():
    repo = request.args.get('repo')
    subprocess.check_call(['rm', '-rf', TCD_APPLICATION_PATH])
    subprocess.call(['rm', '-rf', 'app.report'])
    subprocess.check_call(['git', 'clone', repo, TCD_APPLICATION_PATH])
    while 1:
        out = subprocess.run(['pidof', 'java'], capture_output=True).stdout
        if not out:
            break
        for pid in map(int, out.split()):
            os.kill(pid, signal.SIGKILL)
        time.sleep(5)
    out = subprocess.run(['pidof', 'tail'], capture_output=True).stdout
    for pid in map(int, out.split()):
        os.kill(pid, signal.SIGKILL)
    subprocess.call(['rm', '-rf', 'janusgraph-backup'])
    subprocess.call(['rm', '-rf', '{}/log/*'.format(JANUSGRAPH_PATH)])
    subprocess.call(['rm', '-rf', '{}/run/*'.format(JANUSGRAPH_PATH)])
    subprocess.check_call(['{}/bin/mta-cli'.format(MTA_CLI_PATH), '--input', TCD_APPLICATION_PATH, '--sourceMode', '--target', 'java-ee'])
    subprocess.call(['{}/bin/gremlin-server.sh'.format(JANUSGRAPH_PATH), 'start']) # Why this fails to capture the running server?
    while not os.path.exists('{}/log/gremlin-server.log'.format(JANUSGRAPH_PATH)):
        time.sleep(1)
    subprocess.Popen(['tail', '-f', '{}/log/gremlin-server.log'.format(JANUSGRAPH_PATH)])
    return ""

if __name__ == "__main__":
    app.run(debug=False, host='0.0.0.0', port=8180, threaded=True)
