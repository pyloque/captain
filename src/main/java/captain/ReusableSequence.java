package captain;

import java.util.List;

public class ReusableSequence {

	private String name;
	private int slots;
	private List<Boolean> ids;

	public ReusableSequence(String name, int slots, List<Boolean> ids) {
		this.name = name;
		this.slots = slots;
		this.ids = ids;
	}

	public String getName() {
		return name;
	}

	public int getSlots() {
		return slots;
	}

	public List<Boolean> getIds() {
		return ids;
	}

}
