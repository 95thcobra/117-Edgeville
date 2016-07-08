package edgeville.util;

import edgeville.model.entity.player.Privilege;

/**
 * @author William Talleur <talleurw@gmail.com>
 */
public enum StaffData {

    /* Developers and Management */
    William("Developer", Privilege.ADMIN, 1),
    Sky("Developer", Privilege.ADMIN, 1);

    private String title;
    private Privilege privilege;
    private int crown;

    StaffData(String title, Privilege privilege, int crown) {
        this.title = title;
        this.privilege = privilege;
        this.crown = crown;
    }

    public String getTitle() {
        return title;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public int getCrownId() {
        return crown;
    }
}