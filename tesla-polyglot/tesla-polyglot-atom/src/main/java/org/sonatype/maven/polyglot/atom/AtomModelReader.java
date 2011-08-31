/*
 * Copyright (C) 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.maven.polyglot.atom;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.maven.polyglot.atom.parsing.AtomParser;
import org.sonatype.maven.polyglot.atom.parsing.Project;
import org.sonatype.maven.polyglot.atom.parsing.Tokenizer;
import org.sonatype.maven.polyglot.io.ModelReaderSupport;

/**
 * Reads a <tt>pom.atom</tt> and transforms into a Maven {@link Model}.
 * 
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Component(role = ModelReader.class,hint="atom")
public class AtomModelReader extends ModelReaderSupport {

  public Model read(final Reader input, final Map<String, ?> options) throws IOException {
    assert input != null;

    // Parse the token stream from our pom.atom configuration file.
    Project project = new AtomParser(new Tokenizer(IOUtil.toString(input)).tokenize()).parse();
    return project.toMavenModel();
  }
}
