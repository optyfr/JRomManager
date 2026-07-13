package jrm.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Tree} data structure.
 */
@DisplayName("Tree data structure tests")
class TreeTest {

	private Tree<String> tree;

	@BeforeEach
	void setUp() {
		tree = new Tree<>("root");
	}

	@Test
	@DisplayName("should create tree with root node")
	void shouldCreateTreeWithRootNode() {
		assertThat(tree.getRoot()).isNotNull();
		assertThat(tree.getRoot().getData()).isEqualTo("root");
	}

	@Test
	@DisplayName("should have root with no parent")
	void shouldHaveRootWithNoParent() {
		assertThat(tree.getRoot().getParent()).isNull();
	}

	@Test
	@DisplayName("should have root with no children initially")
	void shouldHaveRootWithNoChildrenInitially() {
		assertThat(tree.getRoot().getChildCount()).isZero();
	}

	@Test
	@DisplayName("should add child to root")
	void shouldAddChildToRoot() {
		var child = tree.getRoot().addChild("child1");

		assertThat(child).isNotNull();
		assertThat(child.getData()).isEqualTo("child1");
		assertThat(child.getParent()).isEqualTo(tree.getRoot());
		assertThat(tree.getRoot().getChildCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("should add multiple children to root")
	void shouldAddMultipleChildrenToRoot() {
		tree.getRoot().addChild("child1");
		tree.getRoot().addChild("child2");
		tree.getRoot().addChild("child3");

		assertThat(tree.getRoot().getChildCount()).isEqualTo(3);
	}

	@Test
	@DisplayName("should add nested children")
	void shouldAddNestedChildren() {
		var child1 = tree.getRoot().addChild("child1");
		var grandchild1 = child1.addChild("grandchild1");
		var grandchild2 = child1.addChild("grandchild2");

		assertThat(child1.getChildCount()).isEqualTo(2);
		assertThat(grandchild1.getParent()).isEqualTo(child1);
		assertThat(grandchild2.getParent()).isEqualTo(child1);
		assertThat(grandchild1.getData()).isEqualTo("grandchild1");
	}

	@Test
	@DisplayName("should iterate over children")
	void shouldIterateOverChildren() {
		tree.getRoot().addChild("child1");
		tree.getRoot().addChild("child2");
		tree.getRoot().addChild("child3");

		var childData = new java.util.ArrayList<String>();
		for (var child : tree.getRoot()) {
			childData.add(child.getData());
		}

		assertThat(childData).containsExactly("child1", "child2", "child3");
	}

	@Test
	@DisplayName("should update node data")
	void shouldUpdateNodeData() {
		var child = tree.getRoot().addChild("original");
		child.setData("updated");

		assertThat(child.getData()).isEqualTo("updated");
	}

	@Test
	@DisplayName("should handle deep nesting")
	void shouldHandleDeepNesting() {
		var level1 = tree.getRoot().addChild("level1");
		var level2 = level1.addChild("level2");
		var level3 = level2.addChild("level3");
		var level4 = level3.addChild("level4");

		assertThat(level4.getData()).isEqualTo("level4");
		assertThat(level4.getParent()).isEqualTo(level3);
		assertThat(level3.getParent()).isEqualTo(level2);
		assertThat(level2.getParent()).isEqualTo(level1);
		assertThat(level1.getParent()).isEqualTo(tree.getRoot());
	}

	@Test
	@DisplayName("should create tree with null root data")
	void shouldCreateTreeWithNullRootData() {
		var nullTree = new Tree<String>(null);

		assertThat(nullTree.getRoot()).isNotNull();
		assertThat(nullTree.getRoot().getData()).isNull();
	}

	@Test
	@DisplayName("should create tree with complex data types")
	void shouldCreateTreeWithComplexDataTypes() {
		var intTree = new Tree<Integer>(42);
		assertThat(intTree.getRoot().getData()).isEqualTo(42);

		var child = intTree.getRoot().addChild(100);
		assertThat(child.getData()).isEqualTo(100);
	}

	@Test
	@DisplayName("should maintain parent-child relationships correctly")
	void shouldMaintainParentChildRelationshipsCorrectly() {
		var child1 = tree.getRoot().addChild("child1");
		var child2 = tree.getRoot().addChild("child2");
		var grandchild = child1.addChild("grandchild");

		assertThat(grandchild.getParent()).isEqualTo(child1);
		assertThat(child1.getParent()).isEqualTo(tree.getRoot());
		assertThat(child2.getParent()).isEqualTo(tree.getRoot());
		assertThat(tree.getRoot().getParent()).isNull();
	}

	@Test
	@DisplayName("should allow multiple children with same data")
	void shouldAllowMultipleChildrenWithSameData() {
		tree.getRoot().addChild("same");
		tree.getRoot().addChild("same");
		tree.getRoot().addChild("same");

		assertThat(tree.getRoot().getChildCount()).isEqualTo(3);
	}
}
