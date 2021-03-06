/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.extension.script.resources;

import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.io.File;
import java.io.IOException;

/**
 * @author tbaum
 * @since 16.06.11 21:34
 */
public class GemfileServerResource extends FileServerResource {

    public GemfileServerResource(File file, String property) {
        super(file, property);
    }

    @Override public void delete(final AbstractGraphDatabase gds) throws IOException {
        super.delete(gds);
        new File(getFile().getCanonicalPath() + ".lock").delete();

    }

    @Override public boolean updateFileSystem(final GraphDatabaseAPI gds) throws IOException {
        final boolean changed = super.updateFileSystem(gds);
        new File(getFile().getCanonicalPath() + ".lock").delete();
        return changed;
    }
}
