package org.codehaus.mojo.tidy;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.codehaus.plexus.util.FileUtils.fileRead;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tidy.task.PomTidy;

/**
 * An abstract base class for Mojos of the Tidy plugin. Handles common
 * configuration issues and provides the POM as String.
 */
public abstract class TidyMojo
    extends AbstractMojo
{
    private static final PomTidy POM_TIDY = new PomTidy();

    /**
     * The Maven Project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * Set this to 'true' to skip execution.
     */
    @Parameter( property = "tidy.skip", defaultValue = "false" )
    protected boolean skip;

    /**
     * Perform whatever build-process behavior this <code>Mojo</code> implements using the specified POM.
     *
     * @param pom the project's POM.
     * @throws MojoExecutionException if an unexpected problem occurs.
     *                                Throwing this exception causes a "BUILD ERROR" message to be displayed.
     * @throws MojoFailureException   if an expected problem (such as a compilation failure) occurs.
     *                                Throwing this exception causes a "BUILD FAILURE" message to be displayed.
     */
    protected abstract void executeForPom( String pom )
        throws MojoExecutionException, MojoFailureException;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Tidy is skipped." );
            return;
        }
        String pom = getProjectPom();
        executeForPom( pom );
    }

    /**
     * Returns the project's POM.
     */
    private String getProjectPom()
        throws MojoExecutionException
    {
        try
        {
            return fileRead( getPomFile() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to read the POM.", e );
        }
    }

    /**
     * Returns the file of the POM.
     */
    protected File getPomFile()
    {
        return project.getFile();
    }

    /**
     * Tidy the given POM.
     */
    protected String tidy( String pom )
        throws MojoExecutionException
    {
        try
        {
            return POM_TIDY.tidy( pom );
        }
        catch ( XMLStreamException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
}
