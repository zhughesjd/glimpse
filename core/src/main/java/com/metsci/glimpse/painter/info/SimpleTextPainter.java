/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.painter.info;

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * A painter which displays arbitrary text at a fixed pixel
 * location on the screen.
 *
 * @author ulman
 */
public class SimpleTextPainter extends GlimpsePainterImpl
{
    public enum HorizontalPosition
    {
        Left, Center, Right;
    }

    public enum VerticalPosition
    {
        Bottom, Center, Top;
    }

    private float[] textColor = GlimpseColor.getBlack( );
    private boolean textColorSet = false;
    
    private boolean paintBackground = false;
    private float[] backgroundColor = GlimpseColor.getBlack( 0.3f );
    private boolean backgroundColorSet = false;

    private boolean paintBorder = false;
    private float[] borderColor = GlimpseColor.getWhite( 1f );
    private boolean borderColorSet = false;
    
    private int horizontalPadding = 5;
    private int verticalPadding = 5;

    private HorizontalPosition hPos;
    private VerticalPosition vPos;

    private TextRenderer textRenderer;
    private boolean fontSet = false;
    
    private String sizeText;
    private String text;

    private boolean horizontal = true;
    
    private volatile Font newFont = null;
    private volatile boolean antialias = false;
    
    public SimpleTextPainter( )
    {
        this.newFont = getDefaultBold( 12 );
        this.hPos = HorizontalPosition.Left;
        this.vPos = VerticalPosition.Bottom;
    }

    public SimpleTextPainter setHorizontalLabels( boolean horizontal )
    {
        this.horizontal = horizontal;
        return this;
    }
    
    public SimpleTextPainter setPaintBackground( boolean paintBackground )
    {
        this.paintBackground = paintBackground;
        return this;
    }

    public SimpleTextPainter setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
        return this;
    }
    
    public SimpleTextPainter setPaintBorder( boolean paintBorder )
    {
        this.paintBorder = paintBorder;
        return this;
    }

    public SimpleTextPainter setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
        this.borderColorSet = true;
        return this;
    }

    public SimpleTextPainter setHorizontalPosition( HorizontalPosition hPos )
    {
        this.hPos = hPos;
        return this;
    }

    public SimpleTextPainter setVerticalPosition( VerticalPosition vPos )
    {
        this.vPos = vPos;
        return this;
    }

    public SimpleTextPainter setFont( Font font )
    {
        setFont( font, false );
        return this;
    }

    public SimpleTextPainter setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public SimpleTextPainter setFont( int size, boolean bold )
    {
        setFont( size, bold, false );
        return this;
    }

    public SimpleTextPainter setFont( int size, boolean bold, boolean antialias )
    {
        if ( bold )
        {
            setFont( getDefaultBold( size ), antialias );
        }
        else
        {
            setFont( getDefaultPlain( size ), antialias );
        }

        return this;
    }

    public SimpleTextPainter setSizeText( String sizeText )
    {
        this.sizeText = sizeText;
        return this;
    }

    public SimpleTextPainter setText( String text )
    {
        this.text = text;
        return this;
    }

    public SimpleTextPainter setPadding( int padding )
    {
        this.verticalPadding = padding;
        this.horizontalPadding = padding;
        return this;
    }
    
    public SimpleTextPainter setVerticalPadding( int padding )
    {
        this.verticalPadding = padding;
        return this;
    }

    public SimpleTextPainter setHorizontalPadding( int padding )
    {
        this.horizontalPadding = padding;
        return this;
    }
    
    public SimpleTextPainter setColor( float[] rgba )
    {
        textColor = rgba;
        textColorSet = true;
        return this;
    }

    public SimpleTextPainter setColor( float r, float g, float b, float a )
    {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
        return this;
    }

    public int getVerticalPadding( )
    {
        return verticalPadding;
    }
    
    public int getHorizontalPadding( )
    {
        return horizontalPadding;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.TITLE_FONT ), false );
            fontSet = false;
        }
        
        if ( !textColorSet )
        {
            setColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            textColorSet = false;
        }
        
        if ( !backgroundColorSet )
        {
            setBackgroundColor( laf.getColor( AbstractLookAndFeel.TEXT_BACKGROUND_COLOR ) );
            backgroundColorSet = false;
        }
        
        if ( !borderColorSet )
        {
            setBorderColor( laf.getColor( AbstractLookAndFeel.BORDER_COLOR ) );
            borderColorSet = false;
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }
    
    public Rectangle2D getTextBounds( )
    {
        return sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );
    }
    
    protected void paintToHorizontal( GL gl, int width, int height, Rectangle2D textBounds )
    {
        int xText = horizontalPadding;
        int yText = verticalPadding;

        switch ( hPos )
        {
        case Left:
            xText = ( int ) horizontalPadding;
            break;
        case Center:
            xText = ( int ) ( width / 2d - textBounds.getWidth( ) / 2d );
            break;
        case Right:
            xText = ( int ) ( width - textBounds.getWidth( ) - horizontalPadding );
            break;
        }

        switch ( vPos )
        {
        case Bottom:
            yText = ( int ) verticalPadding;
            break;
        case Center:
            yText = ( int ) ( height / 2d - textBounds.getHeight( ) / 2d );
            break;
        case Top:
            yText = ( int ) ( height - textBounds.getHeight( ) - verticalPadding );
            break;
        }

        if ( this.paintBackground || this.paintBorder )
        {
            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5, -0.5, height - 1 + 0.5, -1, 1 );
            gl.glMatrixMode( GL.GL_MODELVIEW );
            gl.glLoadIdentity( );

            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL.GL_BLEND );

            Rectangle2D bound = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

            int xTextMax = ( int ) ( xText + bound.getWidth( ) + ( bound.getMinX( ) ) - 1 );
            int yTextMax = ( int ) ( yText + bound.getHeight( ) - 3 );

            if(this.paintBackground)
            {
	            // Draw Text Background
	            gl.glColor4fv( backgroundColor, 0 );
	
	            gl.glBegin( GL.GL_QUADS );
	            try
	            {
	                gl.glVertex2f( xText - 0.5f - 2, yText - 0.5f - 2 );
	                gl.glVertex2f( xTextMax + 0.5f + 2, yText - 0.5f - 2 );
	                gl.glVertex2f( xTextMax + 0.5f + 2, yTextMax + 0.5f + 2 );
	                gl.glVertex2f( xText - 0.5f - 2, yTextMax + 0.5f + 2 );
	            }
	            finally
	            {
	                gl.glEnd( );
	            }
            }
            
            if(this.paintBorder)
            {
	            // Draw Text Border
	            gl.glColor4fv( borderColor, 0 );
            	gl.glEnable(GL.GL_LINE_SMOOTH);
	
	            gl.glBegin( GL.GL_LINE_STRIP );
	            try
	            {
	                gl.glVertex2f( xText - 0.5f - 2, yText - 0.5f - 2 );
	                gl.glVertex2f( xTextMax + 0.5f + 2, yText - 0.5f - 2 );
	                gl.glVertex2f( xTextMax + 0.5f + 2, yTextMax + 0.5f + 2 );
	                gl.glVertex2f( xText - 0.5f - 2, yTextMax + 0.5f + 2 );
	                gl.glVertex2f( xText - 0.5f - 2, yText - 0.5f - 2 );
	            }
	            finally
	            {
	                gl.glEnd( );
	            }
            }
        }
        
        gl.glDisable( GL.GL_BLEND );

        textRenderer.beginRendering( width, height );
        try
        {
            textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
            textRenderer.draw( text, xText, yText );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }
    
    protected void paintToVertical( GL gl, int width, int height, Rectangle2D textBounds )
    {
        int xText = horizontalPadding;
        int yText = verticalPadding;
        
        double textWidth = textBounds.getWidth( );
        double textHeight = textBounds.getHeight( );
        
        int halfTextWidth = (int) ( textWidth / 2d );
        int halfTextHeight = (int) ( textHeight / 2d );

        switch ( hPos )
        {
        case Left:
            xText = ( int ) ( horizontalPadding - halfTextWidth + halfTextHeight );
            break;
        case Center:
            xText = ( int ) ( width / 2d - halfTextWidth );
            break;
        case Right:
            xText = ( int ) ( width - halfTextWidth - halfTextHeight - horizontalPadding );
            break;
        }

        switch ( vPos )
        {
        case Bottom:
            yText = ( int ) ( verticalPadding - halfTextHeight + halfTextWidth );
            break;
        case Center:
            yText = ( int ) ( height / 2d - halfTextHeight );
            break;
        case Top:
            yText = ( int ) ( height - halfTextHeight - halfTextWidth - verticalPadding );
            break;
        }
        
        if ( this.paintBackground || this.paintBorder )
        {
            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( 0, width, 0, height, -1, 1 );
            gl.glMatrixMode( GL.GL_MODELVIEW );
            gl.glLoadIdentity( );

            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL.GL_BLEND );

            int buffer = 2;
            
            int xTextMin = ( int ) ( xText + halfTextWidth - halfTextHeight - buffer );
            int yTextMin = ( int ) ( yText + halfTextWidth + halfTextHeight + buffer );
            
            int xTextMax = ( int ) ( xText + halfTextWidth + halfTextHeight + buffer + 3 );
            int yTextMax = ( int ) ( yText - halfTextWidth + halfTextHeight - buffer );

            if(this.paintBackground)
            {
	            // Draw Text Background
	            gl.glColor4fv( backgroundColor, 0 );
	
	            gl.glBegin( GL.GL_QUADS );
	            try
	            {
	                gl.glVertex2f( xTextMin, yTextMin );
	                gl.glVertex2f( xTextMax, yTextMin );
	                gl.glVertex2f( xTextMax, yTextMax );
	                gl.glVertex2f( xTextMin, yTextMax );
	            }
	            finally
	            {
	                gl.glEnd( );
	            }
            }
            
            if(this.paintBorder)
            {
	            // Draw Text Background
	            gl.glColor4fv( borderColor, 0 );
            	gl.glEnable(GL.GL_LINE_SMOOTH);
            	
	            gl.glBegin( GL.GL_LINE_STRIP );
	            try
	            {
	                gl.glVertex2f( xTextMin, yTextMin );
	                gl.glVertex2f( xTextMax, yTextMin );
	                gl.glVertex2f( xTextMax, yTextMax );
	                gl.glVertex2f( xTextMin, yTextMax );
	                gl.glVertex2f( xTextMin, yTextMin );
	            }
	            finally
	            {
	                gl.glEnd( );
	            }
            }
        }
        
        gl.glDisable( GL.GL_BLEND );
        
        textRenderer.beginRendering( width, height );
        try
        {
            double xShift = xText + halfTextWidth;
            double yShift = yText + halfTextHeight;
            
            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glTranslated( xShift, yShift, 0 );
            gl.glRotated( 90, 0, 0, 1.0f );
            gl.glTranslated( -xShift, -yShift, 0 );
            
            textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
            textRenderer.draw( text, xText, yText );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }

    @Override
    protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
    {
        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }
        
        if ( text == null ) return;

        GL gl = context.getGL( );
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        Rectangle2D textBounds = sizeText == null ? textRenderer.getBounds( text ) : textRenderer.getBounds( sizeText );

        if ( horizontal )
        {
            paintToHorizontal( gl, width, height, textBounds );
        }
        else
        {
            paintToVertical( gl, width, height, textBounds );
        }
    }
}
