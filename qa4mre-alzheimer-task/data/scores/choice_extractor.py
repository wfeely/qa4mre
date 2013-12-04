from xml.dom import minidom

def getText(nodelist):
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)

def handleTok(tokenlist):
    texts = ""
    for token in tokenlist:
        texts += " "+ getText(token.childNodes)
    return texts

outfile = open('binary_labels', 'w')
xml = '../12-test-alzheimer/QA4MRE-2012_BIOMEDICAL_GS.xml'
xmldoc = minidom.parse(xml)
itemlist = xmldoc.getElementsByTagName('answer') 
for item in itemlist:
    print item.childNodes[0].nodeValue
    if 'correct' in item.attributes.keys() :
        outfile.write('1\n')
    else:
        outfile.write('0\n')
outfile.close()
