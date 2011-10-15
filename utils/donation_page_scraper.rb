require 'mechanize'
require './db.rb'

agent=Mechanize.new
page=agent.get("https://en.bitcoin.it/wiki/Donation-accepting_organizations_and_projects")
page.root.css("table.wikitable tbody tr").each do |row|
  cells=row.css("td")
  label=cells[0].text.strip
  site=cells[2].css("a").first.attribute("href").to_s.strip
  address=Address.from_webpage(site).first
  puts "#{label} #{site} #{address}"
  unless address.nil?
    a=Address.find_or_create_by_base58hash address
    a.label=label
    a.notes=site
    a.save
  end
end
