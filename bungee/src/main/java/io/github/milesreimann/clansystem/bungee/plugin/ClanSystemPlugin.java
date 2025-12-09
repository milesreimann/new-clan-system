package io.github.milesreimann.clansystem.bungee.plugin;

import io.github.milesreimann.clansystem.api.service.ClanInvitationService;
import io.github.milesreimann.clansystem.api.service.ClanJoinRequestService;
import io.github.milesreimann.clansystem.api.service.ClanPermissionService;
import io.github.milesreimann.clansystem.bungee.command.ClanCommand;
import io.github.milesreimann.clansystem.bungee.config.MainConfig;
import io.github.milesreimann.clansystem.bungee.database.MySQLDatabase;
import io.github.milesreimann.clansystem.bungee.mapper.ClanInvitationMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanJoinRequestMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanMemberMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanPermissionMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanRoleMapper;
import io.github.milesreimann.clansystem.bungee.mapper.ClanRolePermissionMapper;
import io.github.milesreimann.clansystem.bungee.repository.ClanInvitationRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanJoinRequestRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanMemberRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanPermissionRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanRolePermissionRepository;
import io.github.milesreimann.clansystem.bungee.repository.ClanRoleRepository;
import io.github.milesreimann.clansystem.bungee.service.ClanInvitationServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanJoinRequestServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanMemberServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanPermissionServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanRolePermissionServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanRoleServiceImpl;
import io.github.milesreimann.clansystem.bungee.service.ClanServiceImpl;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Miles R.
 * @since 29.11.2025
 */
@Getter
public class ClanSystemPlugin extends Plugin {
    private MainConfig config;
    private MySQLDatabase database;
    private ClanServiceImpl clanService;
    private ClanMemberServiceImpl clanMemberService;
    private ClanPermissionService clanPermissionService;
    private ClanRolePermissionServiceImpl clanRolePermissionService;
    private ClanRoleServiceImpl clanRoleService;
    private ClanInvitationService clanInvitationService;
    private ClanJoinRequestService clanJoinRequestService;

    @Override
    public void onEnable() {
        config = new MainConfig();

        database = MySQLDatabase.defaultDatabase();
        database.connect();

        ClanRepository clanRepository = new ClanRepository(database, new ClanMapper());
        clanRepository.createTable();

        ClanRoleRepository clanRoleRepository = new ClanRoleRepository(database, new ClanRoleMapper());
        clanRoleRepository.createTableAndTrigger();
        clanRepository.addRoleForeignKeys();

        ClanMemberRepository clanMemberRepository = new ClanMemberRepository(database, new ClanMemberMapper());
        clanMemberRepository.createTable();

        ClanPermissionRepository clanPermissionRepository = new ClanPermissionRepository(database, new ClanPermissionMapper());
        clanPermissionRepository.createTableAndPermissions();

        ClanRolePermissionRepository clanRolePermissionRepository = new ClanRolePermissionRepository(database, new ClanRolePermissionMapper());
        clanRolePermissionRepository.createTable();

        ClanInvitationRepository clanInvitationRepository = new ClanInvitationRepository(database, new ClanInvitationMapper());
        clanInvitationRepository.createTableAndEvent();

        ClanJoinRequestRepository clanJoinRequestRepository = new ClanJoinRequestRepository(database, new ClanJoinRequestMapper());
        clanJoinRequestRepository.createTableAndEvent();

        clanRoleService = new ClanRoleServiceImpl(clanRoleRepository);
        clanPermissionService = new ClanPermissionServiceImpl(clanPermissionRepository);
        clanRolePermissionService = new ClanRolePermissionServiceImpl(clanRolePermissionRepository, clanPermissionService, clanRoleService);
        clanMemberService = new ClanMemberServiceImpl(clanMemberRepository, clanRoleService);
        clanService = new ClanServiceImpl(clanRepository, clanRoleService, clanMemberService, clanRolePermissionService);
        clanInvitationService = new ClanInvitationServiceImpl(clanInvitationRepository, clanMemberService);
        clanJoinRequestService = new ClanJoinRequestServiceImpl(clanJoinRequestRepository, clanMemberService);

        clanService.registerDeleteObserver(clanMemberService.getClanDeleteObserver());
        clanService.registerDeleteObserver(clanRoleService.getClanDeleteObserver());
        clanRoleService.registerDeleteObserver(clanRolePermissionService.getClanRoleDeleteObserver());
        clanRoleService.registerInheritObserver(clanRolePermissionService.getClanRoleInheritObserver());

        getProxy().getPluginManager().registerCommand(this, new ClanCommand(this));
    }

    @Override
    public void onDisable() {
        database.disconnect();
        database = null;
    }
}
