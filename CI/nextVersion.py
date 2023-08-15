#!/usr/bin/python

import sys

# main
def main(tag):
    if "SNAPSHOT" in tag:
        tag = tag.replace("-SNAPSHOT", "")
    tagParts = tag.split(".")
    bumped = tagParts[0] + "." + tagParts[1] + "." + str(int(tagParts[2]) + 1)
    bumped += "-SNAPSHOT"
    print bumped
# here start main
main(sys.argv[1])