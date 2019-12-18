package edu.yang.controller;

import edu.yang.entity.User;
import edu.yang.entity.YugiohCard;
import edu.yang.entity.YugiohCardHistory;
import edu.yang.persistence.ProjectDao;
import edu.yang.service.ProductDetails;
import edu.yang.service.TcgPlayerAPI;
import edu.yang.service.YugiohCardProcessor;
import edu.yang.service.YugiohCardSetsFileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple servlet to add Cards to the db
 * @author Yang
 */

@WebServlet(
        urlPatterns = {"/addEditedCard"}
)

public class AddEditedCard extends HttpServlet {

    //logger
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String fileName = "cardSets.txt";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //card dao being used
        ProjectDao newYugiohCardDao = new ProjectDao(YugiohCard.class);
        ProjectDao tsDao = new ProjectDao(YugiohCardHistory.class);
        ProjectDao userDao = new ProjectDao(User.class);

        Map<String, Object> userInputs = new HashMap<>();

        //get current tmstamp
        Date date = new Date();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);

        User loggedInUser = (User) userDao.getByProperty("userName", req.getRemoteUser());

        userInputs.put("cardName", req.getParameter("cardName"));
        userInputs.put("cardType", req.getParameter("cardType"));
        userInputs.put("cardRarity", req.getParameter("cardRarity"));
        userInputs.put("cardEdition", req.getParameter("cardEdition"));
        userInputs.put("cardSet", req.getParameter("cardSet"));
        userInputs.put("cardIndex", req.getParameter("cardIndex"));
        userInputs.put("cardQuantity", Integer.parseInt(req.getParameter("cardQuantity")));
        userInputs.put("user", loggedInUser);

        String input = req.getParameter("param");
        int cardId = Integer.parseInt(input);

        YugiohCard currentCard = (YugiohCard)newYugiohCardDao.getById(cardId);

        //check for change
        if (!currentCard.getCardName().equalsIgnoreCase(req.getParameter("cardName"))
                && !currentCard.getCardType().equalsIgnoreCase(req.getParameter("cardType"))
                && !currentCard.getCardRarity().equalsIgnoreCase(req.getParameter("cardRarity"))
                && !currentCard.getCardEdition().equalsIgnoreCase(req.getParameter("cardEdition"))
                && !currentCard.getCardSet().equalsIgnoreCase(req.getParameter("cardSet"))
                && !currentCard.getCardIndex().equalsIgnoreCase(req.getParameter("cardIndex"))
                && currentCard.getQuantity() != Integer.parseInt(req.getParameter("cardQuantity")))
        {

            YugiohCardProcessor newHelper = new YugiohCardProcessor();
            YugiohCard newYugiohCard = newHelper.cardProcessor(userInputs);

            //check if the card already exists and update the quantity
            for (YugiohCard card : loggedInUser.getCards()) {
                if (card.equals(newYugiohCard)) {
                    YugiohCard updateCard = (YugiohCard)newYugiohCardDao.getById(card.getId());
                    updateCard.setStatus(card.getStatus() + 1);
                    newYugiohCardDao.saveOrUpdate(updateCard);

                } else  {

                    YugiohCardHistory entry = new YugiohCardHistory(newYugiohCard.getPrice(), newYugiohCard, ts);
                    newYugiohCard.addEntry(entry);
                    int id = newYugiohCardDao.insert(newYugiohCard);
                    int entryId = tsDao.insert(entry);
                }
            }

        } else {
            req.setAttribute("message", "Cannot add card to database, please check input fields and enter in again.");
            RequestDispatcher dispatcher = req.getRequestDispatcher("/editCard.jsp");
            dispatcher.forward(req, resp);
        }

        req.setAttribute("cards", loggedInUser.getCards());
        RequestDispatcher dispatcher = req.getRequestDispatcher("/home.jsp");
        dispatcher.forward(req, resp);
    }

}