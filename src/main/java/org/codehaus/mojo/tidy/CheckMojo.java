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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Checks that the <code>pom.xml</code> is tidy. Fails the build if <code>mvn tidy:pom</code> would
 * create a different <code>pom.xml</code> than the current one.
 */
@Mojo( name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckMojo
    extends TidyMojo
{
    @Override
    protected void executeForPom( String pom )
        throws MojoExecutionException, MojoFailureException
    {
        String tidyPom = tidy( pom );
        if ( !pom.equals( tidyPom ) )
        {
            throw new MojoFailureException(
                "The POM violates the code style. Please format it by running `mvn tidy:pom`." );
        }
    }
}
