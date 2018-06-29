package projects.Chandra_and_Toueg.nodes.messages;

import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.messages.Message;

public class ProposeValueMsg extends Message {

	public CTNode C;
	public int R;
	public int TS;
	public int value;
	public ProposeValueMsg(CTNode c, int r, int ts, int value) {
		this.C = c;
		this.R = r;
		this.TS = ts;
		this.value = value;
	}

	@Override
	public ProposeValueMsg clone() {
		return new ProposeValueMsg(this.C, this.R, this.TS, this.value);
	}
}
