package com.io7m.moduledemo;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class WhitelistModuleDemo
{
  private WhitelistModuleDemo()
  {

  }

  public static void main(
    final String[] args)
  {
    {
      final ModuleLayer boot_layer = ModuleLayer.boot();
      boot_layer.modules().forEach(mm -> {
        System.out.println("boot layer: " + mm);
      });

      final Set<String> whitelist =
        Set.of("java.base");

      final ModuleFinder whitelist_finder =
        new ModuleFinder()
        {
          @Override
          public Optional<ModuleReference> find(
            final String name)
          {
            if (whitelist.contains(name)) {
              return boot_layer.configuration()
                .findModule(name)
                .map(ResolvedModule::reference);
            }

            return Optional.empty();
          }

          @Override
          public Set<ModuleReference> findAll()
          {
            return boot_layer.configuration()
              .modules()
              .stream()
              .filter(m -> whitelist.contains(m.name()))
              .map(ResolvedModule::reference)
              .collect(Collectors.toSet());
          }
        };

      final Configuration cf =
        boot_layer.configuration()
          .resolve(
            whitelist_finder,
            ModuleFinder.of(),
            Set.of("java.base"));

      final ClassLoader scl =
        ClassLoader.getSystemClassLoader();
      final ModuleLayer layer =
        boot_layer.defineModulesWithOneLoader(cf, scl);

      layer.modules().forEach(mm -> {
        System.out.println("app layer: " + mm);
      });
    }
  }
}
