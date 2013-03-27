package ru.spbstu.telematics.flowgen.openflow.rules;


public class Command {

	public enum Action {
		FLOW_ADD,
		FLOW_REMOVE
	}

	public enum RuleGroup {
		RULE_GATEWAY,
		RULE_BROADCAST,
		RULE_SUBNET,
		RULE_VM
	}

	public enum Type {

		// action FLOW_ADD
		FLOW_ADD_GATEWAY,
		FLOW_ADD_BROADCAST,
		FLOW_ADD_SUBNET,
		FLOW_ADD_FIRST_VM,
		FLOW_ADD_ANOTHER_VM,

		// action FLOW_REMOVE
		FLOW_REMOVE_GATEWAY,
		FLOW_REMOVE_BROADCAST,
		FLOW_REMOVE_SUBNET,
		FLOW_REMOVE_LAST_VM,
		FLOW_REMOVE_ANOTHER_VM
	}

	public static Action getAction(Type type) {
		if (type == Type.FLOW_ADD_GATEWAY ||
				type == Type.FLOW_ADD_BROADCAST ||
				type == Type.FLOW_ADD_SUBNET ||
				type == Type.FLOW_ADD_FIRST_VM ||
				type == Type.FLOW_ADD_ANOTHER_VM) {

			return Action.FLOW_ADD;

		} else if (type == Type.FLOW_REMOVE_GATEWAY ||
				type == Type.FLOW_REMOVE_BROADCAST ||
				type == Type.FLOW_REMOVE_SUBNET ||
				type == Type.FLOW_REMOVE_LAST_VM ||
				type == Type.FLOW_REMOVE_ANOTHER_VM) {

			return Action.FLOW_REMOVE;

		}

		throw new IllegalArgumentException("Unknown command type");
	}

	public static RuleGroup getRuleGroup(Type type) {
		if (type == Type.FLOW_ADD_GATEWAY ||
				type == Type.FLOW_REMOVE_GATEWAY) {

			return RuleGroup.RULE_GATEWAY;

		} else if (type == Type.FLOW_ADD_BROADCAST ||
				type == Type.FLOW_REMOVE_BROADCAST) {

			return RuleGroup.RULE_BROADCAST;

		}
		if (type == Type.FLOW_ADD_SUBNET ||
				type == Type.FLOW_REMOVE_SUBNET) {

			return RuleGroup.RULE_SUBNET;

		}
		if (type == Type.FLOW_ADD_FIRST_VM ||
				type == Type.FLOW_ADD_ANOTHER_VM ||
				type == Type.FLOW_REMOVE_LAST_VM ||
				type == Type.FLOW_REMOVE_ANOTHER_VM) {

			return RuleGroup.RULE_VM;

		}

		throw new IllegalArgumentException("Unknown command type");
	}

}
