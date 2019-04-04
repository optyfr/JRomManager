package jrm.ui.profile.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.ui.basic.AbstractNGTreeNode;

public class CatVerNode extends AbstractNGTreeNode
{

	final private CatVer catver;
	
	final private Map<String,CategoryNode> categoryNodeCache = new HashMap<>();
	
	public CatVerNode(final CatVer catver)
	{
		this.catver = catver;
	}

	public CategoryNode getNode(Category cat)
	{
		CategoryNode node;
		if(null==(node=categoryNodeCache.get(cat.getPropertyName())))
			categoryNodeCache.put(cat.getPropertyName(), node=new CategoryNode(cat));
		return node;
	}
	
	@Override
	public Object getUserObject()
	{
		return catver.getUserObject();
	}

	@Override
	public boolean isSelected()
	{
		return catver.isSelected();
	}

	@Override
	public void setSelected(boolean selected)
	{
		catver.setSelected(selected);
	}

	@Override
	public CategoryNode getChildAt(int childIndex)
	{
		return getNode(catver.getListCategories().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return catver.getListCategories().size();
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return catver.getListCategories().indexOf(((CategoryNode)node).category);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return catver.getListCategories().size()==0;
	}

	@Override
	public Enumeration<CategoryNode> children()
	{
		return new Enumeration<CategoryNode>()
		{
			private Iterator<Category> iterator = catver.getListCategories().iterator();

			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public CategoryNode nextElement()
			{
				return getNode(iterator.next());
			}
		};
	}
	
	public final class CategoryNode extends AbstractNGTreeNode
	{
		final private Category category;
		
		final private Map<String,SubCategoryNode> subcategoryNodeCache = new HashMap<>();
		public CategoryNode(final Category category)
		{
			this.category = category;
		}

		public SubCategoryNode getNode(SubCategory subcat)
		{
			SubCategoryNode node;
			if(null==(node=subcategoryNodeCache.get(subcat.getPropertyName())))
				subcategoryNodeCache.put(subcat.getPropertyName(), node=new SubCategoryNode(subcat));
			return node;
		}

		@Override
		public Object getUserObject()
		{
			return category.getUserObject();
		}

		@Override
		public boolean isSelected()
		{
			return category.isSelected();
		}

		@Override
		public void setSelected(boolean selected)
		{
			category.setSelected(selected);
			
		}

		@Override
		public SubCategoryNode getChildAt(int childIndex)
		{
			return getNode(category.getListSubCategories().get(childIndex));
		}

		@Override
		public int getChildCount()
		{
			return category.getListSubCategories().size();
		}

		@Override
		public CatVerNode getParent()
		{
			return CatVerNode.this;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return category.getListSubCategories().indexOf(((SubCategoryNode)node).subcategory);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return category.getListSubCategories().size()==0;
		}

		@Override
		public Enumeration<SubCategoryNode> children()
		{
			return new Enumeration<SubCategoryNode>()
			{
				private Iterator<SubCategory> iterator = category.getListSubCategories().iterator();
				
				@Override
				public SubCategoryNode nextElement()
				{
					return getNode(iterator.next());
				}
				
				@Override
				public boolean hasMoreElements()
				{
					return iterator.hasNext();
				}
			};
		}
		
		public final class SubCategoryNode extends AbstractNGTreeNode
		{
			final private SubCategory subcategory;
			
			public SubCategoryNode(final SubCategory subcategory)
			{
				this.subcategory = subcategory;
			}

			@Override
			public Object getUserObject()
			{
				return subcategory.getUserObject();
			}

			@Override
			public boolean isSelected()
			{
				return subcategory.isSelected();
			}

			@Override
			public void setSelected(boolean selected)
			{
				subcategory.setSelected(selected);
			}

			@Override
			public TreeNode getChildAt(int childIndex)
			{
				return null;
			}

			@Override
			public int getChildCount()
			{
				return 0;
			}

			@Override
			public CategoryNode getParent()
			{
				return CategoryNode.this;
			}

			@Override
			public int getIndex(TreeNode node)
			{
				return 0;
			}

			@Override
			public boolean getAllowsChildren()
			{
				return false;
			}

			@Override
			public boolean isLeaf()
			{
				return true;
			}

			@Override
			public Enumeration<? extends TreeNode> children()
			{
				return null;
			}
		}

	}
	

}
