package jrm.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Tree<T>
{
	private @Getter Node<T> root;

	public Tree(T rootData)
	{
		root = new Node<>(rootData);
	}

	public static class Node<T> implements Iterable<Node<T>>
	{
		private @Getter @Setter T data;
		private @Getter Node<T> parent = null;
		private List<Node<T>> children;

		public Node(T data)
		{
			this.data = data;
			this.children = new ArrayList<>();
		}

		public Node<T> addChild(T child)
		{
			final var childNode = new Node<>(child);
			childNode.parent = this;
			this.children.add(childNode);
			return childNode;
		}
		
		public int getChildCount()
		{
			return children.size();
		}

		@Override
		public Iterator<Node<T>> iterator()
		{
			return children.iterator();
		}
		
	}
}
