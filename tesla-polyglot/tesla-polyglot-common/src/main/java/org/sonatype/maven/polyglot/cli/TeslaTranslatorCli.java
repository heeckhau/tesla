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

package org.sonatype.maven.polyglot.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.maven.polyglot.TeslaModelTranslator;

public class TeslaTranslatorCli {
  private final DefaultPlexusContainer container;

  private final TeslaModelTranslator translator;

  public TeslaTranslatorCli(ClassWorld classWorld) throws Exception {
    if (classWorld == null) {
      classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
    }

    ContainerConfiguration cc = new DefaultContainerConfiguration()
      .setClassWorld(classWorld)
      .setName("translator");
    
    container = new DefaultPlexusContainer(cc);
    translator = container.lookup(TeslaModelTranslator.class);
  }

  public TeslaTranslatorCli() throws Exception {
    this(null);
  }

  public int run(final String[] args) throws Exception {
    if (args == null || args.length != 2) {
      System.out.println("usage: translate <input-file> <output-file>");
      return -1;
    }

    File input = new File(args[0]).getCanonicalFile();
    File output = new File(args[1]).getCanonicalFile();

    System.out.println("Translating " + input + " -> " + output);

    translate(input, output);

    return 0;
  }

  public void translate(final File input, final File output) throws IOException {
    assert input != null;
    assert output != null;

    translate(input.toURI().toURL(), output.toURI().toURL());
  }

  public void translate(final URL input, final URL output) throws IOException {
    assert input != null;
    assert output != null;

    translator.translate(input, output);
  }

  public static void main(final String[] args) throws Exception {
    int result = main(args, null);
    System.exit(result);
  }

  public static int main(final String[] args, final ClassWorld classWorld) throws Exception {
    assert classWorld != null;
    return new TeslaTranslatorCli(classWorld).run(args);
  }
}