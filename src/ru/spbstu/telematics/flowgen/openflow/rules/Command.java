package ru.spbstu.telematics.flowgen.openflow.rules;


public class Command {

	public enum Action {
		FlowAdd,
		FlowRemove
	}

	public enum RuleGroup {
		RuleGateway,
		RuleBroadcast,
		RuleSubnet,
		RuleVm
	}

	public enum Type {

		// action FlowAdd
		FlowAddGateway,
		FlowAddBroadcast,
		FlowAddSubnet,
		FlowAddFirstVm,
		FlowAddAnotherVm,

		// action FlowRemove
		FlowRemoveGateway,
		FlowRemoveBroadcast,
		FlowRemoveSubnet,
		FlowRemoveLastVm,
		FlowRemoveAnotherVm
	}

	public static Action getAction(Type type) {
		if (type == Type.FlowAddGateway ||
				type == Type.FlowAddBroadcast ||
				type == Type.FlowAddSubnet ||
				type == Type.FlowAddFirstVm ||
				type == Type.FlowAddAnotherVm) {

			return Action.FlowAdd;

		} else if (type == Type.FlowRemoveGateway ||
				type == Type.FlowRemoveBroadcast ||
				type == Type.FlowRemoveSubnet ||
				type == Type.FlowRemoveLastVm ||
				type == Type.FlowRemoveAnotherVm) {

			return Action.FlowRemove;

		}

		throw new IllegalArgumentException("Unknown command type");
	}

	public static RuleGroup getRuleGroup(Type type) {
		if (type == Type.FlowAddGateway ||
				type == Type.FlowRemoveGateway) {

			return RuleGroup.RuleGateway;

		} else if (type == Type.FlowAddBroadcast ||
				type == Type.FlowRemoveBroadcast) {

			return RuleGroup.RuleBroadcast;

		}
		if (type == Type.FlowAddSubnet ||
				type == Type.FlowRemoveSubnet) {

			return RuleGroup.RuleSubnet;

		}
		if (type == Type.FlowAddFirstVm ||
				type == Type.FlowAddAnotherVm ||
				type == Type.FlowRemoveLastVm ||
				type == Type.FlowRemoveAnotherVm) {

			return RuleGroup.RuleVm;

		}

		throw new IllegalArgumentException("Unknown command type");
	}

}
