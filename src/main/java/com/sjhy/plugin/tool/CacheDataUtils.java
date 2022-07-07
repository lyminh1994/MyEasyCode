package com.sjhy.plugin.tool;

import com.intellij.database.psi.DbTable;
import com.intellij.psi.PsiClass;
import lombok.Data;

import java.util.List;

/**
 * Cache data utility class
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class CacheDataUtils {
    private volatile static CacheDataUtils cacheDataUtils;

    /**
     * Singleton pattern
     */
    public static CacheDataUtils getInstance() {
        if (cacheDataUtils == null) {
            synchronized (CacheDataUtils.class) {
                if (cacheDataUtils == null) {
                    cacheDataUtils = new CacheDataUtils();
                }
            }
        }
        return cacheDataUtils;
    }

    private CacheDataUtils() {
    }

    /**
     * Currently selected table
     */
    private DbTable selectDbTable;
    /**
     * All selected tables
     */
    private List<DbTable> dbTableList;

    /**
     * Selected class
     */
    private PsiClass selectPsiClass;

    /**
     * All selected tables
     */
    private List<PsiClass> psiClassList;
}
