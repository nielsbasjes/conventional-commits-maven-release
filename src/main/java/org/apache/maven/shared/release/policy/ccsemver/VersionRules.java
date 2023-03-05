package org.apache.maven.shared.release.policy.ccsemver;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.shared.release.policy.ccsemver.config.ProjectVersionPolicyConfig;
import org.apache.maven.shared.release.policy.ccsemver.config.io.xpp3.CCSemverVersionPolicyConfigXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The set of rules that determine from the commit history what the next version should be.
 */
public class VersionRules
{
    private static final Logger LOGGER = LoggerFactory.getLogger( VersionRules.class );

    private final Pattern tagPattern;

    private final List<Pattern> majorUpdatePatterns = new ArrayList<>();
    private final List<Pattern> minorUpdatePatterns = new ArrayList<>();

    public VersionRules( String config )
    {
        int patternFlags = Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES;

        // The default assumes then entire tag is what we need
        String tagRegex = "^(\\d+\\.\\d+\\.\\d+)$";

        // https://www.conventionalcommits.org/en/v1.0.0/
        majorUpdatePatterns.add( Pattern.compile( "^[a-zA-Z]+!(?:\\([a-zA-Z\\d_-]+\\))?: .*$", patternFlags ) );
        majorUpdatePatterns.add( Pattern.compile( "^BREAKING CHANGE:.*$", patternFlags ) );
        minorUpdatePatterns.add( Pattern.compile( "^feat(?:\\([a-zA-Z\\d_-]+\\))?: .*$", patternFlags ) );

        if ( config != null && !config.trim().isEmpty() )
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream( config.getBytes( UTF_8 ) );
            CCSemverVersionPolicyConfigXpp3Reader configReader = new CCSemverVersionPolicyConfigXpp3Reader();
            try
            {
                ProjectVersionPolicyConfig semverConfig = configReader.read( inputStream );

                String semverConfigVersionTag = semverConfig.getVersionTag();
                if ( semverConfigVersionTag != null && !semverConfigVersionTag.trim().isEmpty() )
                {
                    tagRegex = semverConfigVersionTag;
                }

                if ( !semverConfig.getMajorRules().isEmpty() || !semverConfig.getMinorRules().isEmpty() )
                {
                    majorUpdatePatterns.clear();
                    for ( String majorRule : semverConfig.getMajorRules() )
                    {
                        majorUpdatePatterns.add( Pattern.compile( majorRule, patternFlags ) );
                    }
                    minorUpdatePatterns.clear();
                    for ( String minorRule : semverConfig.getMinorRules() )
                    {
                        minorUpdatePatterns.add( Pattern.compile( minorRule, patternFlags ) );
                    }
                }
            }
            catch ( IOException | XmlPullParserException e )
            {
                throw new IllegalArgumentException( "Unable to load the CCSemverVersionPolicyConfig: ", e );
            }
        }
        tagPattern = Pattern.compile( tagRegex, Pattern.MULTILINE );
    }

    public Version.Element getMaxElementSinceLastVersionTag( CommitHistory commitHistory )
    {
        boolean needMinorUpdate = false;
        for ( String change : commitHistory.getChanges() )
        {
            if ( isMajorUpdate( change ) )
            {
                LOGGER.debug( "MAJOR: \"{}\"", change );
                return Version.Element.MAJOR;
            }
            else
            if ( isMinorUpdate( change ) )
            {
                LOGGER.debug( "MINOR: \"{}\"", change );
                needMinorUpdate = true;
            }
        }

        if ( needMinorUpdate )
        {
            return Version.Element.MINOR;
        }
        if ( commitHistory.getLastVersionTag() != null )
        {
            LOGGER.debug( "PATCH: Tag {}", commitHistory.getLastVersionTag() );
            return Version.Element.PATCH;
        }
        return null;
    }

    public boolean isMajorUpdate( String input )
    {
        return matchesAny( majorUpdatePatterns, input );
    }

    public boolean isMinorUpdate( String input )
    {
        return matchesAny( minorUpdatePatterns, input );
    }

    private boolean matchesAny( List<Pattern> patterns, String input )
    {
        for ( Pattern pattern : patterns )
        {
            Matcher matcher = pattern.matcher( input );
            if ( matcher.find() )
            {
                return true;
            }
        }
        return false;
    }

    public Pattern getTagPattern()
    {
        return tagPattern;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append( "Conventional Commits config:\n" );
        result.append( "  VersionTag:\n" );
        result.append( "    >>>" ).append( tagPattern ).append( "<<<\n" );
        result.append( "  MajorRules:\n" );
        for ( Pattern majorUpdatePattern : majorUpdatePatterns )
        {
            result.append( "    >>>" ).append( majorUpdatePattern ).append( "<<<\n" );
        }
        result.append( "  Minor Rules:\n" );
        for ( Pattern minorUpdatePattern : minorUpdatePatterns )
        {
            result.append( "    >>>" ).append( minorUpdatePattern ).append( "<<<\n" );
        }
        return result.toString();
    }
}
