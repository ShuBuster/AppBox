package custom;

import android.content.Context;

import com.Atlas.framework.MenuActivity;


public class FabriqueMenu {
	
	/**
	 * Premier maillon de la fabrique qui re�oit l'appel du client et d�termine
	 * quel objet creer (ici un filtre) pour r�pondre � cette demande.
	 * 
	 * @param critere
	 *            la chaine de caractere qui represente le menu
	 * @param context
	 * @return Menu Type commun � plusieurs objet qui permet un meilleur
	 *         d�couplage.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static Menu create(String critere,Context context) throws IllegalArgumentException, InstantiationException, IllegalAccessException {

		switch(critere){
		case "jungleH": return new MenuJungleH(context);
		case "jungleV": return new MenuJungleV(context);
		case "oceanH": return new MenuOceanH(context);
		}
		throw new IllegalArgumentException(
				"Argument should be jungleh, jungleV, oceanH ");
	}
}
