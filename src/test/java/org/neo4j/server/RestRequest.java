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
package org.neo4j.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.neo4j.server.rest.domain.JsonHelper;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.server.rest.web.PropertyValueException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

public class RestRequest {
    private final URI baseUri;
    private final Client client;
    private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    public RestRequest( URI baseUri ) {
        this( baseUri, null, null );
    }

    public RestRequest( URI baseUri, String username, String password ) {
        this.baseUri = uriWithoutSlash( baseUri );
        client = Client.create();
        if ( username != null ) client.addFilter( new HTTPBasicAuthFilter( username, password ) );

    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    private RestRequest( URI uri, Client client ) {
        this.baseUri = uriWithoutSlash( uri );
        this.client = client;
    }

    private URI uriWithoutSlash( URI uri ) {
        String uriString = uri.toString();
        return uriString.endsWith( "/" ) ? uri( uriString.substring( 0, uriString.length() - 1 ) ) : uri;
    }

    public static String encode( Object value ) {
        if ( value == null ) return "";
        try {
            return URLEncoder.encode( value.toString(), "utf-8" ).replaceAll( "\\+", "%20" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( e );
        }
    }


    private Builder builder( String path ) {
        WebResource resource = client.resource( uri( pathOrAbsolute( path ) ) );
        return resource.accept( mediaType );
    }

    private String pathOrAbsolute( String path ) {
        if ( path.startsWith( "http://" ) ) return path;
        return baseUri + "/" + path;
    }

    public ClientResponse get( String path ) {
        return builder( path ).get( ClientResponse.class );
    }

    public ClientResponse delete( String path ) {
        return builder( path ).delete( ClientResponse.class );
    }

    public ClientResponse post( String path, String data ) {
        return post(path,data,mediaType);
    }
    public ClientResponse post( String path, String data, MediaType mediaType ) {
        Builder builder = builder( path );
        if ( data != null ) {
            builder = builder.entity( data, mediaType );
        }
        return builder.post( ClientResponse.class );
    }


    public ClientResponse put( String path, String data ) {
        Builder builder = builder( path );
        if ( data != null ) {
            builder = builder.entity( data, MediaType.APPLICATION_JSON_TYPE );
        }
        return builder.put( ClientResponse.class );
    }


    public Object toEntity( ClientResponse response ) throws PropertyValueException {
        return JsonHelper.jsonToSingleValue( entityString( response ) );
    }

    public Map<?, ?> toMap( ClientResponse response ) throws JsonParseException {
        return JsonHelper.jsonToMap(entityString(response));
    }

    private String entityString( ClientResponse response ) {
        return response.getEntity( String.class );
    }

    public boolean statusIs( ClientResponse response, Response.StatusType status ) {
        return response.getStatus() == status.getStatusCode();
    }

    public boolean statusOtherThan( ClientResponse response, Response.StatusType status ) {
        return !statusIs( response, status );
    }

    public RestRequest with( String uri ) {
        return new RestRequest( uri( uri ), client );
    }

    private URI uri( String uri ) {
        try {
            return new URI( uri );
        } catch ( URISyntaxException e ) {
            throw new RuntimeException( e );
        }
    }

    public URI getUri() {
        return baseUri;
    }
}
