
package chb.segment;

/**
 * CWCharacter is a wrapper class for one character
 * @author Hongbao Chen
 *
 */
public class CWCharacter {

	public Character Content;
	public Long Index;
	
	private CWCharacter() { }
	public static CWCharacter CreateCWChar(Character _c, Long _i) {
		CWCharacter character = new CWCharacter();
		character.Content = _c;
		character.Index = _i;
		
		return character;
	}
}
