/*
 * User: tom
 * Date: Jul 30, 2002
 * Time: 10:55:08 AM
 */
package net.sourceforge.pmd.cpd;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GST {

    private int minimumTileSize;
    private TokenSets tokenSets;

    public GST(TokenSets tokenSets, int minimumTileSize) {
        this.minimumTileSize = minimumTileSize;
        this.tokenSets = tokenSets;
    }
		
	public Results crunch(CPDListener listener) {
        Results results = new Results();
        Occurrences occ =new Occurrences(tokenSets, listener);

        while (!occ.isEmpty()) {
			listener.update("Tiles left to be crunched " + occ.size());

            // add any tiles over the minimum size to the results
			listener.update("Adding large tiles to results");
            for (Iterator i = occ.getTiles(); i.hasNext();) {
                Tile tile = (Tile)i.next();
                if (tile.getTokenCount() >= minimumTileSize) {
                    for (Iterator j = occ.getOccurrences(tile); j.hasNext();) {
                        results.addTile(tile, (TokenEntry)j.next());
                    }
                }
            }

            Occurrences newOcc = new Occurrences(listener);
            int tilesSoFar = 0;
            int totalTiles = occ.size();
            for (Iterator i = occ.getTiles(); i.hasNext();) {
                tilesSoFar++;
                Tile tile = (Tile)i.next();
                if (!newOcc.containsAnyTokensIn(tile)) {
                    expandTile(occ, newOcc, tile, listener, tilesSoFar, totalTiles);
                }
            }
            occ = newOcc;
        }
        return results;
    }

    public Results crunch() {
		return crunch(new CPDNullListener());
    }

    private void expandTile(Occurrences oldOcc, Occurrences newOcc, Tile tile, CPDListener listener, int tilesSoFar, int totalTiles) {
        for (Iterator i = oldOcc.getOccurrences(tile); i.hasNext();) {
            TokenEntry tok = (TokenEntry)i.next();
            TokenList tokenSet = tokenSets.getTokenList(tok);
            if (tokenSet.hasTokenAfter(tile, tok)) {
                TokenEntry token = (TokenEntry)tokenSet.get(tok.getIndex() + tile.getTokenCount());
                // make sure the next token hasn't already been used in an occurrence
                if (!newOcc.contains(token)) {
                    Tile newTile = tile.copy();
                    newTile.add(token);
                    newOcc.addTile(newTile, tok);
					listener.addedNewTile(newTile, tilesSoFar, totalTiles);
                }
            }
        }
        newOcc.deleteSoloTiles();
    }
}
