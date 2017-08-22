package com.eci.cosw.springbootsecureapi.config;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Santiago Carrillo
 * 8/21/17.
 */
public class JwtFilter
    extends GenericFilterBean
{

    public void doFilter( final ServletRequest servletRequest, final ServletResponse response, final FilterChain filterChain )
        throws IOException, ServletException
    {

/*        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) response;
        final String authHeader = request.getHeader( "authorization" );

        if ( "OPTIONS".equals( request.getMethod() ) )
        {
            response.setStatus( HttpServletResponse.SC_OK );

            filterChain.doFilter( servletRequest, response );
        }
        else
        {

            if ( authHeader == null || !authHeader.startsWith( "Bearer " ) )
            {
                throw new ServletException( "Missing or invalid Authorization header" );
            }

            final String token = authHeader.substring( 7 );

            try
            {
                final Claims claims = Jwts.parser().setSigningKey( "secretkey" ).parseClaimsJws( token ).getBody();
                request.setAttribute( "claims", claims );
            }
            catch ( final SignatureException e )
            {
                throw new ServletException( "Invalid token" );
            }

            filterChain.doFilter( servletRequest, response );
        }*/
    }
}
