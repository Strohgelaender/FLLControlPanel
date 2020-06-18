package de.robogo.fll.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.util.Callback;

public class JuryScreenSettings extends ScreenSettings {

	private final Map<String, Callback<Jury, String>> juryReplacement = new HashMap<>();

	public JuryScreenSettings() {
		juryReplacement.put("room", Jury::getRoom);
		juryReplacement.put("shortName", jury -> jury.getJuryType().getShortName());
		juryReplacement.put("longName", jury -> jury.getJuryType().getLongName());
		juryReplacement.put("num", jury -> String.valueOf(jury.getNum()));
	}

	@Override
	public Set<String> getReplacements() {
		Set<String> values = new HashSet<>(super.getReplacements());
		values.addAll(juryReplacement.keySet());
		return values;
	}

	public String applyReplacement(final String replacement, Jury jury) {
		if (juryReplacement.containsKey(replacement))
			return juryReplacement.get(replacement).call(jury);
		return super.applyReplacement(replacement);
	}
}
