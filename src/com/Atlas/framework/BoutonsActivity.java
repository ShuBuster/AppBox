package com.Atlas.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import boutons.Bouton;
import boutons.NextActivityListener;

public class BoutonsActivity extends Activity {

	EditText edit = null;
	LinearLayout stock = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boutons);

		/* Bouton couleur perso */
		final Button bouton = Bouton.createButton(this, R.color.rose);
		final RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent_view);
		parent.addView(bouton);

		/* Recuperation elements */
		edit = (EditText) findViewById(R.id.editText);
		stock = (LinearLayout) findViewById(R.id.stock);

		/* Bouton home de retour au menu */
		final Button home = (Button) findViewById(R.id.home);
		home.setOnClickListener(new NextActivityListener(home, null,
				BoutonsActivity.this, MainActivity.class));

		/* Bouton de creation */
		final Button creer = (Button) findViewById(R.id.creer);
		creer.setTextSize(30);
		creer.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				edit = (EditText) findViewById(R.id.editText);
				final String nomBouton = edit.getText().toString();
				final Button b = Bouton.createButton(
						getApplicationContext(),
						getApplicationContext().getResources().getColor(
								R.color.bleu1));
				stock.addView(b);

			}
		});

	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		final Button home = (Button) findViewById(R.id.home);
		if (Build.VERSION.SDK_INT >= 16) {
			//home.setBackground(getResources().getDrawable(R.drawable.home));
			Method methodBackgroung;
				try {
					methodBackgroung = View.class.getMethod("setBackground",
							Drawable.class);
					methodBackgroung.invoke(home, getResources().getDrawable(R.drawable.home));
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		} else {
			home.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.home));
		}

	}
}
