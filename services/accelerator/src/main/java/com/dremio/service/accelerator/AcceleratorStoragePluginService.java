/*
 * Copyright (C) 2017 Dremio Corporation
 *
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
package com.dremio.service.accelerator;

import javax.inject.Provider;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dremio.exec.store.dfs.FileSystemConfig;
import com.dremio.exec.store.dfs.FileSystemPlugin;
import com.dremio.exec.util.ImpersonationUtil;
import com.dremio.service.Service;

/**
 * Initializes the AcceleratorStoragePlugin which involves lazy loading the storage plugin and creating the corresponding
 * directory
 */
public class AcceleratorStoragePluginService implements Service {

  private final Provider<FileSystemPlugin> acceleratorStoragePluginProvider;

  public AcceleratorStoragePluginService(Provider<FileSystemPlugin> acceleratorStoragePluginProvider) {
    this.acceleratorStoragePluginProvider = acceleratorStoragePluginProvider;
  }

  @Override
  public void start() throws Exception {
    final FileSystemPlugin fileSystemPlugin = acceleratorStoragePluginProvider.get();
    final FileSystem fs = fileSystemPlugin.getFS(ImpersonationUtil.getProcessUserName());
    fs.mkdirs(new Path(fileSystemPlugin.getId().<FileSystemConfig>getConfig().getPath()));
  }

  @Override
  public void close() throws Exception {
  }
}
