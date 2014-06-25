File pomFile = new File( basedir, 'pom.xml' )
File expectedPomFile = new File( basedir, 'pom-expected.xml' )

assert pomFile.getText() == expectedPomFile.getText()
