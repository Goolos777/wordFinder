package ed.od.ua;

import ed.od.ua.reader.GitsPublicOwenFindRepo;
import ed.od.ua.reader.GitsPublicOwenRepoFindFiles;
import ed.od.ua.service.Manager;

import java.util.Arrays;

public class Main {


    public static void main(final String[] args) {

        GitsPublicOwenFindRepo gitsPublicOwnerRepo = new GitsPublicOwenFindRepo();
        gitsPublicOwnerRepo.addUser("Goolos777");
        gitsPublicOwnerRepo.addUser("RaviKharatmal");
        GitsPublicOwenRepoFindFiles gitsPublicOwenRepoFindFiles = new GitsPublicOwenRepoFindFiles();
        String url = "https://api.github.com";
        final int  controlLength = 3;
        final int findCounter = 3;
        String login  = "Goolos777";
        String pass  = "790411";
        Manager manager = new Manager(url, Arrays.asList(gitsPublicOwnerRepo,gitsPublicOwenRepoFindFiles ), controlLength, login, pass );
        manager.find(findCounter);
    }


}
