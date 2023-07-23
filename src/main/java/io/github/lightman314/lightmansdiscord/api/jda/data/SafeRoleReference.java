package io.github.lightman314.lightmansdiscord.api.jda.data;

import net.dv8tion.jda.api.entities.Role;

public class SafeRoleReference {

    private final Role role;
    private SafeRoleReference(Role role) { this.role = role; }
    public static SafeRoleReference of(Role role) { if(role != null) return new SafeRoleReference(role); return null; }

    public final String getID() { return this.role.getId(); }
    public final String getName() { return this.role.getName(); }
    public final boolean isPublicRole() { return this.role.isPublicRole(); }

}
