package ru.spbstu.telematics.flowgen.openflow.rules;


public class Command {

	public enum Group {
		FLOW_ADD,
		FLOW_REMOVE
	}

	public enum Type {

		// FLOW_ADD group
		FLOW_ADD_GATEWAY,
		FLOW_ADD_BROADCAST,
		FLOW_ADD_SUBNET,
		FLOW_ADD_FIRST_VM,
		FLOW_ADD_ANOTHER_VM,

		// FLOW_REMOVE group
		FLOW_REMOVE_GATEWAY,
		FLOW_REMOVE_BROADCAST,
		FLOW_REMOVE_SUBNET,
		FLOW_REMOVE_LAST_VM,
		FLOW_REMOVE_ANOTHER_VM
	}

	public static Group getGroup(Type type) {
		if (type == Type.FLOW_ADD_GATEWAY ||
				type == Type.FLOW_ADD_BROADCAST ||
				type == Type.FLOW_ADD_SUBNET ||
				type == Type.FLOW_ADD_FIRST_VM ||
				type == Type.FLOW_ADD_ANOTHER_VM) {

			return Group.FLOW_ADD;

		} else if (type == Type.FLOW_REMOVE_GATEWAY ||
				type == Type.FLOW_REMOVE_BROADCAST ||
				type == Type.FLOW_REMOVE_SUBNET ||
				type == Type.FLOW_REMOVE_LAST_VM ||
				type == Type.FLOW_REMOVE_ANOTHER_VM) {

			return Group.FLOW_REMOVE;

		}

		throw new IllegalArgumentException("Unknown command type");
	}

}
