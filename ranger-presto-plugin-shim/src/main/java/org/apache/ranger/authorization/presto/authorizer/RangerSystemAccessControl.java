/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ranger.authorization.presto.authorizer;

import com.facebook.presto.common.CatalogSchemaName;
import com.facebook.presto.spi.CatalogSchemaTableName;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.security.*;
import org.apache.ranger.plugin.classloader.RangerPluginClassLoader;

import javax.inject.Inject;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RangerSystemAccessControl
  implements SystemAccessControl {
  private static final String RANGER_PLUGIN_TYPE = "presto";
  private static final String RANGER_PRESTO_AUTHORIZER_IMPL_CLASSNAME = "org.apache.ranger.authorization.presto.authorizer.RangerSystemAccessControl";

  final private RangerPluginClassLoader rangerPluginClassLoader;
  final private SystemAccessControl systemAccessControlImpl;

  @Inject
  public RangerSystemAccessControl(RangerConfig config) {
    try {
      rangerPluginClassLoader = RangerPluginClassLoader.getInstance(RANGER_PLUGIN_TYPE, this.getClass());

      @SuppressWarnings("unchecked")
      Class<SystemAccessControl> cls = (Class<SystemAccessControl>) Class.forName(RANGER_PRESTO_AUTHORIZER_IMPL_CLASSNAME, true, rangerPluginClassLoader);

      activatePluginClassLoader();

      Map<String, String> configMap = new HashMap<>();
      if (config.getKeytab() != null && config.getPrincipal() != null) {
        configMap.put("ranger.keytab", config.getKeytab());
        configMap.put("ranger.principal", config.getPrincipal());
      }
      systemAccessControlImpl = cls.getDeclaredConstructor(Map.class).newInstance(configMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      deactivatePluginClassLoader();
    }
  }



  @Override
  public void checkCanSetSystemSessionProperty(Identity identity, AccessControlContext context, String propertyName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanSetSystemSessionProperty(identity, context, propertyName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denySetSystemSessionProperty(propertyName);
    }
  }

  @Override
  public void checkCanAccessCatalog(Identity identity, AccessControlContext context, String catalogName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanAccessCatalog(identity, context, catalogName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyCatalogAccess(catalogName);
    }
  }

  @Override
  public Set<String> filterCatalogs(Identity identity,AccessControlContext context,  Set<String> catalogs) {
    return catalogs;
  }

  @Override
  public void checkCanCreateSchema(Identity identity, AccessControlContext context, CatalogSchemaName schema) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanCreateSchema(identity, context, schema);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyCreateSchema(schema.getSchemaName());
    }
  }

  @Override
  public void checkCanDropSchema(Identity identity,AccessControlContext context,  CatalogSchemaName schema) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanDropSchema(identity,context, schema);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyDropSchema(schema.getSchemaName());
    }
  }

  @Override
  public void checkCanRenameSchema(Identity identity,AccessControlContext context,  CatalogSchemaName schema, String newSchemaName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanRenameSchema(identity,context, schema, newSchemaName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyRenameSchema(schema.getSchemaName(), newSchemaName);
    }
  }

  @Override
  public void checkCanShowSchemas(Identity identity, AccessControlContext context, String catalogName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanShowSchemas(identity, context, catalogName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyShowSchemas();
    }
  }

  @Override
  public Set<String> filterSchemas(Identity identity,AccessControlContext context,  String catalogName, Set<String> schemaNames) {
    return schemaNames;
  }

  @Override
  public void checkCanCreateTable(Identity identity,AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanCreateTable(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyCreateTable(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanDropTable(Identity identity,AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanDropTable(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyDropTable(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanRenameTable(Identity identity, AccessControlContext context, CatalogSchemaTableName table, CatalogSchemaTableName newTable) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanRenameTable(identity,context, table, newTable);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyRenameTable(table.getSchemaTableName().getTableName(), newTable.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanShowTablesMetadata(Identity identity,AccessControlContext context,  CatalogSchemaName schema) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanShowTablesMetadata(identity,context, schema);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyShowTablesMetadata(schema.getSchemaName());
    }
  }

  @Override
  public Set<SchemaTableName> filterTables(Identity identity,AccessControlContext context,  String catalogName, Set<SchemaTableName> tableNames) {
    return tableNames;
  }

  @Override
  public void checkCanAddColumn(Identity identity,AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanAddColumn(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyAddColumn(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanDropColumn(Identity identity,AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanDropColumn(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyDropColumn(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanRenameColumn(Identity identity,AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanRenameColumn(identity, context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyRenameColumn(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanSelectFromColumns(Identity identity,AccessControlContext context,  CatalogSchemaTableName table, Set<String> columns) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanSelectFromColumns(identity, context, table, columns);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denySelectColumns(table.getSchemaTableName().getTableName(), columns);
    }
  }

  @Override
  public void checkCanInsertIntoTable(Identity identity, AccessControlContext context, CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanInsertIntoTable(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyInsertTable(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanDeleteFromTable(Identity identity, AccessControlContext context,  CatalogSchemaTableName table) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanDeleteFromTable(identity,context, table);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyDeleteTable(table.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanCreateView(Identity identity,AccessControlContext context,  CatalogSchemaTableName view) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanCreateView(identity, context, view);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyCreateView(view.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanDropView(Identity identity,AccessControlContext context,  CatalogSchemaTableName view) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanDropView(identity, context, view);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyDropView(view.getSchemaTableName().getTableName());
    }
  }

  @Override
  public void checkCanCreateViewWithSelectFromColumns(Identity identity,AccessControlContext context,  CatalogSchemaTableName table, Set<String> columns) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanCreateViewWithSelectFromColumns(identity,context, table, columns);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denyCreateViewWithSelect(table.getSchemaTableName().getTableName(), identity);
    }
  }

  @Override
  public void checkCanSetCatalogSessionProperty(Identity identity, AccessControlContext context, String catalogName, String propertyName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanSetCatalogSessionProperty(identity,context, catalogName, propertyName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denySetCatalogSessionProperty(catalogName, propertyName);
    }
  }

  @Override
  public void checkCanRevokeTablePrivilege(Identity identity, AccessControlContext context, Privilege privilege, CatalogSchemaTableName table, PrestoPrincipal revokee, boolean grantOptionFor) {

  }
  @Override
  public void checkCanSetUser(Identity identity, AccessControlContext context, Optional<Principal> principal, String userName) {
    try {
      activatePluginClassLoader();
      systemAccessControlImpl.checkCanSetUser(null, null, principal, userName);
    } catch (AccessDeniedException e) {
      deactivatePluginClassLoader();
      throw e;
    } catch (Exception e) {
      deactivatePluginClassLoader();
      AccessDeniedException.denySetUser(principal, userName);
    }
  }

  @Override
  public void checkQueryIntegrity(Identity identity, AccessControlContext context, String query) {

  }

  private void activatePluginClassLoader() {
    if (rangerPluginClassLoader != null) {
      rangerPluginClassLoader.activate();
    }
  }

  private void deactivatePluginClassLoader() {
    if (rangerPluginClassLoader != null) {
      rangerPluginClassLoader.deactivate();
    }
  }
}
