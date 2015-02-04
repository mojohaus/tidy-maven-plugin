# Personal mirror of the Tidy Maven Plugin

[![Build Status](https://secure.travis-ci.org/stefanbirkner/tidy-maven-plugin.png)](https://travis-ci.org/stefanbirkner/tidy-maven-plugin)

This is my personal mirror of the repository of the [Tidy Maven Plugin](http://mojo.codehaus.org/tidy-maven-plugin/). It has been created by the command

    git svn clone https://svn.codehaus.org/mojo/trunk/mojo/tidy-maven-plugin tidy-maven-plugin

It has two additional files

* `README.md` – This file with a short introduction to the repository.
* `.gitignore` – Don't track Maven's `target` folder.
* `.travis.yml` – Configuration file for the Travis CI server.

## How to sync

This repository is synced with the original repository by

    git svn rebase

The Git commit can be committed back to Subversion by

    git svn dcommit
