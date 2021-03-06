package process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* Copyright (c) 2012 Kevin L. Stern
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * The Damerau-Levenshtein Algorithm is an extension to the Levenshtein
 * Algorithm which solves the edit distance problem between a source string and
 * a target string with the following operations:
 * 
 * <ul>
 * <li>Character Insertion</li>
 * <li>Character Deletion</li>
 * <li>Character Replacement</li>
 * <li>Adjacent Character Swap</li>
 * </ul>
 * 
 * Note that the adjacent character swap operation is an edit that may be
 * applied when two adjacent characters in the source string match two adjacent
 * characters in the target string, but in reverse order, rather than a general
 * allowance for adjacent character swaps.
 * <p>
 * 
 * This implementation allows the client to specify the costs of the various
 * edit operations with the restriction that the cost of two swap operations
 * must not be less than the cost of a delete operation followed by an insert
 * operation. This restriction is required to preclude two swaps involving the
 * same character being required for optimality which, in turn, enables a fast
 * dynamic programming solution.
 * <p>
 * 
 * The running time of the Damerau-Levenshtein algorithm is O(n*m) where n is
 * the length of the source string and m is the length of the target string.
 * This implementation consumes O(n*m) space.
 * 
 * @author Kevin L. Stern
 */
public class DamerauLevensheinDistance {
  private final float deleteCost, insertCost, replaceCost, swapCost;

  /**
   * Constructor.
   * 
   * @param deleteCost
   *          the cost of deleting a character.
   * @param insertCost
   *          the cost of inserting a character.
   * @param replaceCost
   *          the cost of replacing a character.
   * @param swapCost
   *          the cost of swapping two adjacent characters.
   */
  public DamerauLevensheinDistance(float deleteCost, float insertCost,
                                     float replaceCost, float swapCost) {
    /*
     * Required to facilitate the premise to the algorithm that two swaps of the
     * same character are never required for optimality.
     */
    if (2 * swapCost < insertCost + deleteCost) {
      throw new IllegalArgumentException("Unsupported cost assignment");
    }
    this.deleteCost = deleteCost;
    this.insertCost = insertCost;
    this.replaceCost = replaceCost;
    this.swapCost = swapCost;
  }

  /**
   * Compute the Damerau-Levenshtein distance between the specified source
   * string and the specified target string.
   */
  public float execute(String source, String target) {
    if (source.length() == 0) {
      return target.length() * insertCost;
    }
    if (target.length() == 0) {
      return source.length() * deleteCost;
    }
    float[][] table = new float[source.length()][target.length()];
    Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
    if (source.charAt(0) != target.charAt(0)) {
      table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
    }
    sourceIndexByCharacter.put(source.charAt(0), 0);
    for (int i = 1; i < source.length(); i++) {
      float deleteDistance = table[i - 1][0] + deleteCost;
      float insertDistance = (i + 1) * deleteCost + insertCost;
      float matchDistance = i * deleteCost
          + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
      table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
                             matchDistance);
    }
    for (int j = 1; j < target.length(); j++) {
      float deleteDistance = (j + 1) * insertCost + deleteCost;
      float insertDistance = table[0][j - 1] + insertCost;
      float matchDistance = j * insertCost
          + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
      table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
                             matchDistance);
    }
    for (int i = 1; i < source.length(); i++) {
      int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0
          : -1;
      for (int j = 1; j < target.length(); j++) {
        Integer candidateSwapIndex = sourceIndexByCharacter.get(target
            .charAt(j));
        int jSwap = maxSourceLetterMatchIndex;
        float deleteDistance = table[i - 1][j] + deleteCost;
        float insertDistance = table[i][j - 1] + insertCost;
        float matchDistance = table[i - 1][j - 1];
        if (source.charAt(i) != target.charAt(j)) {
          matchDistance += replaceCost;
        } else {
          maxSourceLetterMatchIndex = j;
        }
        float swapDistance;
        if (candidateSwapIndex != null && jSwap != -1) {
          int iSwap = candidateSwapIndex;
          float preSwapCost;
          if (iSwap == 0 && jSwap == 0) {
            preSwapCost = 0;
          } else {
            preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
          }
          swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost
              + (j - jSwap - 1) * insertCost + swapCost;
        } else {
          swapDistance = Integer.MAX_VALUE;
        }
        table[i][j] = Math.min(Math.min(Math
            .min(deleteDistance, insertDistance), matchDistance), swapDistance);
      }
      sourceIndexByCharacter.put(source.charAt(i), i);
    }
    return table[source.length() - 1][target.length() - 1];
  }
  
  
  
  /**
   * Compute the Damerau-Levenshtein distance between the specified source
   * string and the specified target string.
   */
  public float executeTerms(ArrayList<String> source, ArrayList<String> target) {
    if (source.size() == 0) {
      return target.size() * insertCost;
    }
    if (target.size() == 0) {
      return source.size() * deleteCost;
    }
    float[][] table = new float[source.size()][target.size()];
    Map<String, Integer> sourceIndexByCharacter = new HashMap<String, Integer>();
    if (source.get(0).equalsIgnoreCase(target.get(0))) {
      table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
    }
    
    sourceIndexByCharacter.put(source.get(0), 0);
    for (int i = 1; i < source.size(); i++) {
      float deleteDistance = table[i - 1][0] + deleteCost;
      float insertDistance = (i + 1) * deleteCost + insertCost;
      float matchDistance = i * deleteCost
          + (source.get(i) == target.get(0) ? 0 : replaceCost);
      table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
                             matchDistance);
    }
    for (int j = 1; j < target.size(); j++) {
      float deleteDistance = (j + 1) * insertCost + deleteCost;
      float insertDistance = table[0][j - 1] + insertCost;
      float matchDistance = j * insertCost
          + (source.get(0) == target.get(j) ? 0 : replaceCost);
      table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
                             matchDistance);
    }
    for (int i = 1; i < source.size(); i++) {
      int maxSourceLetterMatchIndex = source.get(i) == target.get(0) ? 0
          : -1;
      for (int j = 1; j < target.size(); j++) {
        Integer candidateSwapIndex = sourceIndexByCharacter.get(target.get(j));
        int jSwap = maxSourceLetterMatchIndex;
        float deleteDistance = table[i - 1][j] + deleteCost;
        float insertDistance = table[i][j - 1] + insertCost;
        float matchDistance = table[i - 1][j - 1];
        
        if (!source.get(i).equalsIgnoreCase(target.get(j))) {
          matchDistance += replaceCost;
        } else {
          maxSourceLetterMatchIndex = j;
        }
        
        float swapDistance;
        if (candidateSwapIndex != null && jSwap != -1) {
          int iSwap = candidateSwapIndex;
          float preSwapCost;
          if (iSwap == 0 && jSwap == 0) {
            preSwapCost = 0;
          } else {
            preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
          }
          swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost
              + (j - jSwap - 1) * insertCost + swapCost;
        } else {
          swapDistance = Integer.MAX_VALUE;
        }
        table[i][j] = Math.min(Math.min(Math
            .min(deleteDistance, insertDistance), matchDistance), swapDistance);
      }
      sourceIndexByCharacter.put(source.get(i), i);
    }
    return table[source.size() - 1][target.size() - 1];
  }
  
  public float executeTermsScore(ArrayList<String> source, ArrayList<String> target) {
	    if (source.size() == 0 || target.size() == 0) {
	      return 0;
	    }
	    
	    float[][] table = new float[source.size()][target.size()];
	    Map<String, Integer> sourceIndexByCharacter = new HashMap<String, Integer>();
	    if (source.get(0).equalsIgnoreCase(target.get(0))) {
	      table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
	    }
	    
	    sourceIndexByCharacter.put(source.get(0), 0);
	    for (int i = 1; i < source.size(); i++) {
	      float deleteDistance = table[i - 1][0] + deleteCost;
	      float insertDistance = (i + 1) * deleteCost + insertCost;
	      float matchDistance = i * deleteCost
	          + (source.get(i) == target.get(0) ? 0 : replaceCost);
	      table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
	                             matchDistance);
	    }
	    for (int j = 1; j < target.size(); j++) {
	      float deleteDistance = (j + 1) * insertCost + deleteCost;
	      float insertDistance = table[0][j - 1] + insertCost;
	      float matchDistance = j * insertCost
	          + (source.get(0) == target.get(j) ? 0 : replaceCost);
	      table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
	                             matchDistance);
	    }
	    for (int i = 1; i < source.size(); i++) {
	      int maxSourceLetterMatchIndex = source.get(i) == target.get(0) ? 0
	          : -1;
	      for (int j = 1; j < target.size(); j++) {
	        Integer candidateSwapIndex = sourceIndexByCharacter.get(target.get(j));
	        int jSwap = maxSourceLetterMatchIndex;
	        float deleteDistance = table[i - 1][j] + deleteCost;
	        float insertDistance = table[i][j - 1] + insertCost;
	        float matchDistance = table[i - 1][j - 1];
	        
	        if (!source.get(i).equalsIgnoreCase(target.get(j))) {
	          matchDistance += replaceCost;
	        } else {
	          maxSourceLetterMatchIndex = j;
	        }
	        
	        float swapDistance;
	        if (candidateSwapIndex != null && jSwap != -1) {
	          int iSwap = candidateSwapIndex;
	          float preSwapCost;
	          if (iSwap == 0 && jSwap == 0) {
	            preSwapCost = 0;
	          } else {
	            preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
	          }
	          swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost
	              + (j - jSwap - 1) * insertCost + swapCost;
	        } else {
	          swapDistance = Integer.MAX_VALUE;
	        }
	        table[i][j] = Math.min(Math.min(Math
	            .min(deleteDistance, insertDistance), matchDistance), swapDistance);
	      }
	      sourceIndexByCharacter.put(source.get(i), i);
	    }
	    int length = Math.max(source.size(), target.size());
	    return (float) (table[source.size() - 1][target.size() - 1]*1.0/length);
	  }
}


