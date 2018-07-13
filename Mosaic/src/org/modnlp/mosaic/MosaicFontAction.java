/**
 *  (c) 2014 S Sheehan <shane.sheehan@tcd.ie>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
package org.modnlp.mosaic;

import java.awt.Font;
import prefuse.action.assignment.FontAction;
import prefuse.visual.VisualItem;

/**
 *
 * @author shane
 */
public class MosaicFontAction extends FontAction {
    
    public MosaicFontAction(String group)
    {
        super(group);
    }
    
    @Override
    public Font getFont(VisualItem item){
        Font font = new Font("Tahoma",0, 14);
        return font;
    }
    
}

