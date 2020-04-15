String [] buildLog = new File(basedir, 'build.log')

codeStyleViolations = buildLog.grep(~/^.*The POM violates the code style\..*$/)

assert codeStyleViolations.size > 0: 'Reported POM code style violations are expected in build log'
