package com.mstiles92.bookrules;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mstiles92.bookrules.lib.CraftWrittenBook;
import com.mstiles92.bookrules.lib.WrittenBook;

public class BookRulesCommandExecutor implements CommandExecutor {
	private final BookRulesPlugin plugin;
	
	public BookRulesCommandExecutor(BookRulesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if (!(cs instanceof Player)) {
			cs.sendMessage(plugin.tag + ChatColor.RED + "This command may only be executed by a player.");
			return true;
		}
		Player p = (Player)cs;
		
		if (args.length == 0) {
			if (!p.hasPermission("bookrules.info")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			p.sendMessage(ChatColor.BLUE + "BookRules by " + plugin.getDescription().getAuthors().get(0));
			p.sendMessage(ChatColor.BLUE + "Version " + plugin.getDescription().getVersion());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (!p.hasPermission("bookrules.reload")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			plugin.loadConfig();
			p.sendMessage(plugin.tag + "Files reloaded!");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("get")) {
			if (!p.hasPermission("bookrules.get")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			if (args.length < 2) {
				if (plugin.giveAllBooks(p, true)) {
					p.sendMessage(plugin.tag + "You have received a copy of all registered books.");
				} else {
					p.sendMessage(plugin.tag + ChatColor.RED + "No books defined.");
				}
				return true;
			}
			
			if (plugin.giveBook(p, args[1])) {
				p.sendMessage(plugin.tag + "You have received a copy of the requested book.");
			} else {
				p.sendMessage(plugin.tag + ChatColor.RED + "The specified book could not be found!");
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("add")) {
			if (!p.hasPermission("bookrules.add")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			if (p.getItemInHand().getType() != Material.WRITTEN_BOOK) {
				p.sendMessage(plugin.tag + ChatColor.RED + "This command may only be used while holding a written book.");
				return true;
			}
			
			try {
				WrittenBook book = new CraftWrittenBook(p.getItemInHand());
				plugin.addBook(book);
				p.sendMessage(plugin.tag + "Your book has been added successfully.");
			} catch (Exception e) {
				plugin.log("Exception occurred while constructing a Written Book.");
				e.printStackTrace();
			}
			
			return true;
		}
		
		if (args[0].equalsIgnoreCase("delete")) {
			if (!p.hasPermission("bookrules.delete")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			if (args.length < 2) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You must specify an ID of the book you would like to delete.");
				return true;
			}
			
			if (plugin.deleteBook(args[1])) {
				p.sendMessage(plugin.tag + "Book successfully deleted.");
			} else {
				p.sendMessage(plugin.tag + ChatColor.RED + "The specified book could not be found!");
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("list")) {
			if (!p.hasPermission("bookrules.list")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			List<String> list = plugin.readAllBooks();
			p.sendMessage(plugin.tag + "All registered books:");
			for (String s : list) {
				p.sendMessage(plugin.tag + s);
			}
			
			return true;
		}
		
		if (args[0].equalsIgnoreCase("setauthor")) {
			if (!p.hasPermission("bookrules.setauthor")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			if (args.length < 2) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You must specify an author to change to.");
				return true;
			}
			
			if (p.getInventory().getItemInHand().getType() != Material.WRITTEN_BOOK) {
				p.sendMessage(plugin.tag + ChatColor.RED + "This command may only be performed while holding a written book.");
				return true;
			}
			
			WrittenBook book = null;
			
			try {
				book = new CraftWrittenBook(p.getItemInHand());
			} catch (Exception e) {
				plugin.log("Exception occurred while constructing a WrittenBook");
				e.printStackTrace();
			}
			
			if (book == null) {
				cs.sendMessage(plugin.tag + ChatColor.RED + "An error has occurred.");
				return true;
			}
			
			book.setAuthor(args[1]);
			
			try {
				p.setItemInHand(book.getItemStack(p.getItemInHand().getAmount()));
				p.sendMessage(plugin.tag + "You have successfully changed the author of the currently held book.");
			} catch (Exception e) {
				plugin.log("Exception occurred while returning a Written Book as an ItemStack.");
				e.printStackTrace();
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("settitle")) {
			if (!p.hasPermission("bookrules.settitle")) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			if (args.length < 2) {
				p.sendMessage(plugin.tag + ChatColor.RED + "You must specify a title to change to.");
				return true;
			}
			
			if (p.getInventory().getItemInHand().getType() != Material.WRITTEN_BOOK) {
				p.sendMessage(plugin.tag + ChatColor.RED + "This command may only be performed while holding a written book.");
				return true;
			}
			
			WrittenBook book = null;
			
			try {
				book = new CraftWrittenBook(p.getItemInHand());
			} catch (Exception e) {
				plugin.log("Exception occurred while constructing a WrittenBook");
				e.printStackTrace();
			}
			
			if (book == null) {
				cs.sendMessage(plugin.tag + ChatColor.RED + "An error has occurred.");
				return true;
			}
			
			book.setTitle(args[1]);
			
			try {
				p.setItemInHand(book.getItemStack(p.getItemInHand().getAmount()));
				p.sendMessage(plugin.tag + "You have successfully changed the title of the currently held book.");
			} catch (Exception e) {
				plugin.log("Exception occurred while returning a Written Book as an ItemStack.");
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
	}

}
