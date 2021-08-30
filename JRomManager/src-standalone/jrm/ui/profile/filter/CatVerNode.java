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

	private final CatVer catver;
	
	private final Map<String,CategoryNode> categoryNodeCache = new HashMap<>();
	
	@SuppressWarnings("exports")
	public CatVerNode(final CatVer catver)
	{
		this.catver = catver;
	}

	@SuppressWarnings("exports")
	public CategoryNode getNode(Category cat)
	{
		return categoryNodeCache.computeIfAbsent(cat.getPropertyName(), pptname -> new CategoryNode(cat));
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

	@SuppressWarnings("exports")
	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@SuppressWarnings("exports")
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
		return catver.getListCategories().isEmpty();
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
		private final Category category;
		
		private final Map<String,SubCategoryNode> subcategoryNodeCache = new HashMap<>();
		
		@SuppressWarnings("exports")
		public CategoryNode(final Category category)
		{
			this.category = category;
		}

		@SuppressWarnings("exports")
		public SubCategoryNode getNode(SubCategory subcat)
		{
			return subcategoryNodeCache.computeIfAbsent(subcat.getPropertyName(), pptname -> new SubCategoryNode(subcat));
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

		@SuppressWarnings("exports")
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
			return category.getListSubCategories().isEmpty();
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
			private final SubCategory subcategory;
			
			@SuppressWarnings("exports")
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
			public CategoryNode getParent()
			{
				return CategoryNode.this;
			}

		}

	}
	

}
