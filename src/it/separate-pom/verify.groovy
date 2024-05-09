File pomFile = new File( basedir, 'separate-pom.xml' )
File expectedPomFile = new File( basedir, 'separate-pom-expected.xml' )

assert pomFile.getText() == expectedPomFile.getText()
