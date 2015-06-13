package custom;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;


/**
 * Dernier maillon de la fabrique qui regroupe plusieurs objets sous un m�me
 * type commun Filtre pour un meilleur d�couplage: le Client ne re�oit que ce
 * type commun.
 * 
 * @author Victor, Nicklos
 * 
 */
public interface Menu {

	RelativeLayout[] createMenu(ViewGroup parent);
	void rassembler(int l1, int l2);
	Button addButton(String texte, int place);
	void addTitre(String texte);
	void destroy(int place);
	
}
