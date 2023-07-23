package io.github.lightman314.lightmansdiscord.api.jda.data;

import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nullable;
import java.util.List;

public class SafeMemberReference extends SafeUserReference {

    private final Member member;
    public final Member getMember() { return this.member; }
    private SafeMemberReference(Member member) { super(member.getUser()); this.member = member; }
    @Nullable
    public static SafeMemberReference of(Member member) { if(member != null) return new SafeMemberReference(member); return null; }

    public final int getColor() { return this.member.getColorRaw(); }

    public final String getEffectiveName() { return this.member.getEffectiveName(); }

    public final List<SafeRoleReference> getRoles() { return this.member.getRoles().stream().map(SafeRoleReference::of).toList(); }

}
