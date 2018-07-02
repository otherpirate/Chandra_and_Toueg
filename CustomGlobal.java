/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.Chandra_and_Toueg;

import java.util.Iterator;
import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Runtime;
import sinalgo.tools.logging.Logging;


public class CustomGlobal extends AbstractCustomGlobal{
	
	Logging log = Logging.getLogger("CT.txt");
	
	public boolean hasTerminated() {
		return false;
	}

	int round = 0;
	
	@Override
	public void preRun() {
		log.logln("round,leader,decided,undecided,ack,nack,msg_node,msg_value,msg_ts");	
	}
	
	@Override
	public void preRound() {
		Iterator<Node> nodeIter = Runtime.nodes.iterator();
		while(nodeIter.hasNext()){
			CTNode n = (CTNode) nodeIter.next();
			n.N = Runtime.nodes.size();
			n.halfPlusOne = (n.N/2) + 1;
		}
	}
	
	@Override
	public void postRound() {
		int leader = 0;
		int decided = 0;
		int undecided = 0;
		int acks = 0;
		int nacks = 0;
		int msg_node = -1;
		int msg_value = -1;
		int msg_ts = -1;

		Iterator<Node> nodeIter = Runtime.nodes.iterator();
		while(nodeIter.hasNext()){
			CTNode n = (CTNode) nodeIter.next();
			if (n.isLeader) {
				leader = n.ID;
				if (n.maxProposeValueMsg != null) {
					msg_node = n.maxProposeValueMsg.C.ID;
					msg_value = n.maxProposeValueMsg.value;
					msg_ts = n.maxProposeValueMsg.TS;
				}
			}
			if (n.state == CTNode.DECIDED) {
				decided++;
			}
			if (n.state == CTNode.UNDECIDED) {
				undecided++;
			}
			if (n.isACK) {
				acks++;
			}
			if (n.isNACK) {
				nacks++;
			}
		}
		log.logln("" + round +
				  "," + leader  +
				  "," + decided +	
				  "," + undecided +
				  "," + acks +
				  "," + nacks +
				  "," + msg_node +
				  "," + msg_value + 
				  "," + msg_ts);
		round += 1;

		checkAllDecided(undecided);
	}
	
	private void checkAllDecided(int undecided) {
		if (undecided == 1) {
			Iterator<Node> nodeIter = Runtime.nodes.iterator();
			while(nodeIter.hasNext()){
				CTNode n = (CTNode) nodeIter.next();
				n.nextConsensus();
			}
		}
	}
}