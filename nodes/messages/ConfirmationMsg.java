package projects.Chandra_and_Toueg.nodes.messages;

import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.messages.Message;

public class ConfirmationMsg extends Message {
	
	public CTNode node; 
	public ConfirmationMsg(CTNode node) {
		this.node = node;
	}

	@Override
	public ConfirmationMsg clone() {
		return new ConfirmationMsg(this.node);
	}
}
