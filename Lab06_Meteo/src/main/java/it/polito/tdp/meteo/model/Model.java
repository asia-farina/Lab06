package it.polito.tdp.meteo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private int minimo=10000000;
	List<String> sequenza;
	List<Rilevamento> rilevamenti;
	List<String> localita;
	MeteoDAO meteo;
	
	public Model() {
          meteo=new MeteoDAO();
          localita=meteo.getLocalita();
          rilevamenti=meteo.getAllRilevamenti();
	}

	// of course you can change the String output with what you think works best
	public Map<String,Double> getUmiditaMedia(int mese) {
	    Map <String, Double> mediaPerCitta=new HashMap<String, Double>();
	    for (String s:localita) {
	    	int somma=0;
	    	int cont=0;
	    	for (Rilevamento r:meteo.getAllRilevamentiLocalitaMese(mese, s)) {
	    		somma+=r.getUmidita();
	    		cont++;
	    	}
	    	double media=somma/cont;
	    	mediaPerCitta.put(s, media);
	    }
	    return mediaPerCitta;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		List<String> parziale=new ArrayList<String>();
		ricorsione(parziale, mese, 0, 0);
		List <LocalDate> giorni=meteo.giorni(mese);
		String s="";
		int i=0;
		while (i<sequenza.size()-1) {
			s+=giorni.get(i)+" "+sequenza.get(i)+"\n";
			i++;
		}
		s+="Somma totale= "+minimo;
		minimo=100000;
		sequenza.clear();
		return s;
	}
	
	private void ricorsione (List<String> parziale, int mese, int livello, int somma) {
		if (livello==16) {
			if (somma<minimo) {
			   minimo=somma;
			   sequenza=new ArrayList<String>(parziale);
			}
			return ;
		} else {
			if (deveContinuare(parziale, livello)) {
		    	List<String> parziale1=new ArrayList<String> (parziale);
		    	parziale1.add(parziale1.get(livello-1));
		    	int somma1=somma+getUmidita(parziale1.get(livello-1), livello, mese);
		    	ricorsione(parziale1, mese, livello+1, somma1);
			} else {
			for (String s:localita) {
			if (valida(s,parziale, livello)) {
		    	List<String> parziale1=new ArrayList<String> (parziale);
		    	parziale1.add(s);
		    	int somma1;
		    	if (livello!=0 && !s.equals(parziale.get(livello-1)))
		    	    somma1=somma+getUmidita(s, livello, mese)+COST;
		    	else
		    		somma1=somma+getUmidita(s, livello, mese);
		    	ricorsione(parziale1, mese, livello+1, somma1);
		  }
		}
      }
	  }
	}
	
	private boolean valida (String localita, List<String> parziale, int posizione) {
		if (posizione<6)
			return true;
		int flag=0;
		for (int k=posizione-1; k>=posizione-NUMERO_GIORNI_CITTA_MAX; k--) {
		if (localita.equals(parziale.get(k)))
		    flag++;
		}
		  if (flag==6) {
			  return false;
		  }
		return true;
	}
	
	private boolean deveContinuare (List <String> parziale, int posizione) {
		if (posizione==0)
			return false;
		if (posizione>0 && posizione<3)
		    return true;
		if (parziale.get(posizione-1).equals(parziale.get(posizione-2)) && parziale.get(posizione-1).equals(parziale.get(posizione-3)))
			return false;
		return true;
	}
	
	private int getUmidita (String localita, int giorno, int mese) {
		for (Rilevamento r:rilevamenti) {
			if (r.getLocalita().equals(localita) && r.getData().getDayOfMonth()==giorno && r.getData().getMonthValue()==mese )
				return r.getUmidita();
		}
		return -1;
	}

}
