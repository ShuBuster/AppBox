package services;

/*
 * Projet SIT, @copyright 2015 SAGEM DS
 * Les informations contenues dans ce fichier sont la propri�t� de
 * SAGEM DS et diffus�es � titre confidentiel dans un but sp�cifique.
 * Le destinataire assure la garde et la surveillance de ce fichier et
 * convient qu'il ne sera ni copi� ni reproduit en tout ou partie et
 * que son contenu ne sera r�v�l� en aucune mani�re � aucune personne,
 * except� pour r�pondre au but pour le quel il a �t� transmis.
 * Cette recommandation est applicable � tous les documents g�n�r�s �
 * partir de ce fichier.
 */

/**
 * @author local
 */
public interface Bluetooth_Constants
{

    // Message types sent from the CommService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;

    public static final int MESSAGE_READ = 2;

    public static final int MESSAGE_WRITE = 3;

    public static final int MESSAGE_DEVICE_NAME = 4;

    public static final int MESSAGE_TOAST = 5;

    // Key names received from the CommService Handler
    public static final String DEVICE_NAME = "device_name";

    public static final String TOAST = "toast";
}
