package com.docmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.docmanager.entity.Category;
import com.docmanager.entity.Document;
import com.docmanager.exception.BusinessException;
import com.docmanager.mapper.CategoryMapper;
import com.docmanager.mapper.DocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DocumentMapper documentMapper;

    public List<Category> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    public Category createCategory(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        category.setDeleted(0);
        categoryMapper.insert(category);
        logger.info("分类创建成功: {}", category.getName());
        return category;
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName()).ne(Category::getId, id);
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        categoryMapper.updateById(existing);
        logger.info("分类更新成功: {}", existing.getName());
        return existing;
    }

    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }

        LambdaQueryWrapper<Document> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(Document::getCategoryId, id).eq(Document::getDeleted, 0);
        Long docCount = documentMapper.selectCount(docWrapper);
        if (docCount > 0) {
            throw new BusinessException("该分类下有 " + docCount + " 个文档，无法删除");
        }

        categoryMapper.deleteById(id);
        logger.info("分类删除成功: {}", category.getName());
    }
}
