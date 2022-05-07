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

import sys

description = '''
Prerequisites:
$ pip3 install javatools
$ brew install mermaid-cli

Usage:
$ python3 script/extract.py tcd-windup/target/tcd-windup-0.0.1-SNAPSHOT.jar > models.mmd
$ mmdc -i models.mmd -o docs/models.svg
'''

def get_opts(argv=[]):
    import argparse
    p = argparse.ArgumentParser(epilog=description, formatter_class=argparse.RawTextHelpFormatter)
    p.add_argument('jars', nargs='*', default=[
        '/Users/akihiko/.gradle/caches/modules-2/files-2.1/org.jboss.windup.graph/windup-graph-api/5.1.3.Final/692fb117c2045c1828f2d37cb58fa25d2ff78a25/windup-graph-api-5.1.3.Final.jar',
        '/Users/akihiko/.gradle/caches/modules-2/files-2.1/org.jboss.windup.rules.apps/windup-rules-base-api/5.1.3.Final/f13d393285da76f614cdb55a9c538fdb6bf28911/windup-rules-base-api-5.1.3.Final.jar',
        '/Users/akihiko/.gradle/caches/modules-2/files-2.1/org.jboss.windup.rules.apps/windup-rules-java-api/5.1.3.Final/2404f8baf1f1db6fc1417246640a4bb7316d2e5d/windup-rules-java-api-5.1.3.Final.jar'
    ], help='jar file')
    return p.parse_args(argv)

def models(opts):
    cs = {}
    for file in opts.jars:
        jar = JarInfo(filename=file)
        for e in jar.get_classes():
            c = jar.get_classinfo(e)
            cs[c.get_this()] = c
            # print (c.get_interfaces())
    ms = {}
    for n in cs:
        c = cs[n]
        fs = list(c.get_interfaces())
        ok = False
        while fs:
            f = fs.pop()
            if f == 'org/jboss/windup/graph/model/WindupVertexFrame':
                ok = True
                break
            elif f in cs:
                fs += cs[f].get_interfaces()
            elif f.endswith('Model'):
                print('unknown: %s' % f, file=sys.stderr) 
        if ok:
           ms[c.get_this()] = c
    return ms


def deref(c, v):
    if isinstance(v[1], int):
        return c.deref_const(v[1])
    elif isinstance(v[1], tuple):
        return [c.deref_const(w) for w in v[1]]
    else:
        print('unknown: {}'.format(v), file=sys.stderr)

def klazz(c):
    if isinstance(c, JavaClassInfo):
       c = c.get_this() 
    return c[c.rindex('/')+1:]

def sig(m):
    return m.get_descriptor() if m.get_signature() is None else m.get_signature()

def parse_sig(m):
    s = sig(m)
    card = None
    if 'Ljava/util/' in s or 'Ljava/lang/Iterable' in s:
        card = '*'
    if '<' in s:
        s = s[s.index('<')+1:s.rindex('>')]
    isref = ';' in s
    if (not isref and 'Z' in s) or 'Ljava/lang/Boolean;' in s:
        return (card, 'boolean')
    if (not isref and 'I' in s) or 'Ljava/lang/Integer;' in s:
        return (card, 'int')
    if (not isref and 'F' in s) or 'Ljava/lang/Float;' in s:
        return (card, 'float')
    if (not isref and 'L' in s) or 'Ljava/lang/Long;' in s:
        return (card, 'long')
    if 'Ljava/lang/String;' in s:
        return (card, 'string')
    if '/' in s:
        s = s[s.rindex('/')+1:s.rindex(';')]
    elif ';)' in s:
        s = s[s.rindex('(')+1:s.rindex(';')]
    else:
        s = s[s.rindex(')')+1:s.rindex(';')]
    return (card, s)

def mermaid(ms):
    print('classDiagram')
    for c in ms.values():
        emitted = False
        seen  = set()
        assocs = []
        for m in c.methods:
            prop = None
            assoc = False
            dir = 'OUT'
            for a in m.get_annotations():
                if a.pretty_type() not in ('org.jboss.windup.graph.Adjacency', 'org.jboss.windup.graph.Property'):
                    continue
                if 'label' in a:
                    prop = deref(c, a['label'])
                    assoc = True
                    if 'direction' in a:
                       dir = deref(c, a['direction'])[1]
                elif 'value' in a:
                    prop = deref(c, a['value'])
            if not prop or prop in seen:
                continue
            seen.add(prop)
            card, typ = parse_sig(m)
            if not assoc:
                if not emitted:
                    print ('class %s {' % klazz(c))
                    emitted = True
                if not card:
                    print ('+{} {}'.format(typ, prop))
                else:
                    print ('+List~{}~ {}'.format(typ, prop))
            else:
                assocs.append((prop, card, dir, typ))
        if emitted:
            print ('}')
        else:
            print ('class %s' % klazz(c))
        for prop, card, dir, typ in assocs:
            if dir == 'OUT':
                print('{} o-- {} : {}'.format(typ, klazz(c), prop))
            elif dir == 'IN':
                print('{} o-- {} : {}'.format(klazz(c), typ, prop))
        for i in c.get_interfaces():
            print('{} <-- {}'.format(klazz(i), klazz(c)))

def process(opts):
    ms = models(opts)
    for c in ms.values():
        print ('----- {} -----'.format(c.get_this()))
        for m in c.methods:
            print('{} : {} {}'.format(m.get_name(), sig(m), m.get_annotations()))
            for a in m.get_annotations():
                for k in a:
                    print(a.pretty_type())
                    print('{} -> {}'.format(k, deref(c, a[k])))
    print (ms.keys())

def process(opts):
    ms = models(opts)
    mermaid(ms)

if __name__ == '__main__':
    import json, sys, os

    opts = get_opts(sys.argv[1:])
    print (opts, file=sys.stderr)

    from javatools.jarinfo import JarInfo
    from javatools import JavaClassInfo
    
    res = process(opts)
